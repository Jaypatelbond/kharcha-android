package com.kharcha.tracker.presentation.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.tracker.domain.model.Budget
import com.kharcha.tracker.domain.model.Category
import com.kharcha.tracker.domain.model.TransactionType
import com.kharcha.tracker.domain.repository.BudgetRepository
import com.kharcha.tracker.domain.repository.CategoryRepository
import com.kharcha.tracker.domain.repository.TransactionRepository
import com.kharcha.tracker.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class BudgetUiItem(
    val id: Int,
    val categoryName: String,
    val amount: Double,
    val spent: Double,
    val categoryColor: Int,
    val categoryIconName: String
) {
    val isOverallBudget: Boolean
        get() = categoryName == "All"
        
    val progress: Float
        get() = if (amount > 0) (spent / amount).toFloat() else 0f
}

data class BudgetUiState(
    val selectedMonth: LocalDate = LocalDate.now(),
    val budgets: List<BudgetUiItem> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    
    val showAddDialog: Boolean = false,
    val editingBudget: BudgetUiItem? = null,
    val amountInput: String = "",
    val categoryInput: String = "All", // "All" or Category name
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val analyticsManager: com.kharcha.tracker.data.analytics.AnalyticsManager
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(LocalDate.now())
    private val _dialogState = MutableStateFlow(DialogState())
    
    private data class DialogState(
        val showDialog: Boolean = false,
        val editingBudget: BudgetUiItem? = null,
        val amountInput: String = "",
        val categoryInput: String = "All",
        val error: String? = null
    )

    // Load expense categories
    val expenseCategories: StateFlow<List<Category>> = categoryRepository.getCategoriesByType("EXPENSE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine budgets with spent totals
    val uiState: StateFlow<BudgetUiState> = combine(
        _selectedMonth,
        expenseCategories,
        _dialogState,
        _selectedMonth.flatMapLatest { date ->
            val start = DateUtils.getStartOfMonth(date)
            val end = DateUtils.getEndOfMonth(date)
            val monthStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            
            combine(
                budgetRepository.getBudgetsForMonth(monthStr),
                transactionRepository.getCategoryTotals(start, end),
                transactionRepository.getTotalByTypeAndDateRange(TransactionType.EXPENSE, start, end)
            ) { budgets, categoryTotals, overallSpent ->
                val spentMap = categoryTotals.associate { it.category.name to it.total }
                
                budgets.map { budget ->
                    val spent = if (budget.categoryName == "All") overallSpent else spentMap[budget.categoryName] ?: 0.0
                    val category = expenseCategories.value.find { it.name == budget.categoryName }
                    
                    BudgetUiItem(
                        id = budget.id,
                        categoryName = budget.categoryName,
                        amount = budget.amount,
                        spent = spent,
                        categoryColor = if (budget.categoryName == "All") 0xFF00BFA6.toInt() else category?.color ?: 0xFF808080.toInt(),
                        categoryIconName = if (budget.categoryName == "All") "account_balance_wallet" else category?.iconName ?: "category"
                    )
                }
            }
        }
    ) { date, categories, dialog, budgetItems ->
        BudgetUiState(
            selectedMonth = date,
            budgets = budgetItems.sortedWith(compareBy({ !it.isOverallBudget }, { it.categoryName })),
            expenseCategories = categories,
            showAddDialog = dialog.showDialog,
            editingBudget = dialog.editingBudget,
            amountInput = dialog.amountInput,
            categoryInput = dialog.categoryInput,
            error = dialog.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetUiState())

    fun nextMonth() {
        _selectedMonth.update { it.plusMonths(1) }
    }

    fun previousMonth() {
        _selectedMonth.update { it.minusMonths(1) }
    }

    fun onAddClick() {
        _dialogState.update {
            DialogState(
                showDialog = true,
                categoryInput = "All",
                amountInput = "",
                editingBudget = null,
                error = null
            )
        }
    }

    fun onEditClick(budget: BudgetUiItem) {
        _dialogState.update {
            DialogState(
                showDialog = true,
                categoryInput = budget.categoryName,
                amountInput = budget.amount.toInt().toString(),
                editingBudget = budget,
                error = null
            )
        }
    }

    fun onDeleteClick(budget: BudgetUiItem) {
        viewModelScope.launch {
            val monthStr = _selectedMonth.value.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            budgetRepository.deleteBudget(
                Budget(
                    id = budget.id,
                    categoryName = budget.categoryName,
                    amount = budget.amount,
                    month = monthStr
                )
            )
        }
    }

    fun onAmountChange(amount: String) {
        _dialogState.update { it.copy(amountInput = amount, error = null) }
    }

    fun onCategoryChange(category: String) {
        _dialogState.update { it.copy(categoryInput = category) }
    }

    fun onDialogDismiss() {
        _dialogState.update { it.copy(showDialog = false) }
    }

    fun onSave() {
        val state = _dialogState.value
        val amount = state.amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _dialogState.update { it.copy(error = "Enter a valid positive budget amount") }
            return
        }

        viewModelScope.launch {
            val monthStr = _selectedMonth.value.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val budget = Budget(
                id = state.editingBudget?.id ?: 0,
                categoryName = state.categoryInput,
                amount = amount,
                month = monthStr
            )

            if (state.editingBudget != null) {
                budgetRepository.updateBudget(budget)
            } else {
                // Check duplicate overall/category budget
                val existing = budgetRepository.getBudgetForCategoryAndMonth(state.categoryInput, monthStr)
                if (existing != null) {
                    _dialogState.update { it.copy(error = "A budget for this category already exists for this month") }
                    return@launch
                }
                budgetRepository.insertBudget(budget)
                analyticsManager.logFeatureUsed("budget_created")
            }
            _dialogState.update { it.copy(showDialog = false) }
        }
    }
}
