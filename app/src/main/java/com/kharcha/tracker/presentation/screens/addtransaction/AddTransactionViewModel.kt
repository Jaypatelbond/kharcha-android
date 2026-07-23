package com.kharcha.tracker.presentation.screens.addtransaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.tracker.domain.model.Category
import com.kharcha.tracker.domain.model.PaymentMode
import com.kharcha.tracker.domain.model.Transaction
import com.kharcha.tracker.domain.model.TransactionType
import com.kharcha.tracker.domain.repository.CategoryRepository
import com.kharcha.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val selectedPaymentMode: PaymentMode = PaymentMode.UPI,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val isEditing: Boolean = false,
    val editingTransactionId: Long = 0,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val analyticsManager: com.kharcha.tracker.data.analytics.AnalyticsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<Category>> = _uiState
        .map { it.type }
        .distinctUntilChanged()
        .flatMapLatest { type ->
            categoryRepository.getCategoriesByType(if (type == TransactionType.INCOME) "INCOME" else "EXPENSE")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val id = savedStateHandle.get<Long>("id")
        if (id != null && id > 0) {
            loadTransaction(id)
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            repository.getTransactionById(id)?.let { txn ->
                _uiState.value = AddTransactionUiState(
                    amount = txn.amount.toLong().toString(),
                    type = txn.type,
                    selectedCategory = txn.category,
                    selectedPaymentMode = txn.paymentMode,
                    note = txn.note,
                    date = txn.date,
                    isEditing = true,
                    editingTransactionId = txn.id
                )
            }
        }
    }

    fun onAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(amount = filtered, error = null) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type, selectedCategory = null, error = null) }
    }

    fun onCategorySelect(category: Category) {
        _uiState.update { it.copy(selectedCategory = category, error = null) }
    }

    fun onPaymentModeSelect(mode: PaymentMode) {
        _uiState.update { it.copy(selectedPaymentMode = mode) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }
        if (state.selectedCategory == null) {
            _uiState.update { it.copy(error = "Please select a category") }
            return
        }

        viewModelScope.launch {
            val transaction = Transaction(
                id = if (state.isEditing) state.editingTransactionId else 0,
                amount = amount,
                type = state.type,
                category = state.selectedCategory,
                paymentMode = state.selectedPaymentMode,
                note = state.note,
                date = state.date
            )
            if (state.isEditing) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
                analyticsManager.logTransactionAdded(
                    amount = transaction.amount,
                    category = transaction.category.name,
                    type = transaction.type.name
                )
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
