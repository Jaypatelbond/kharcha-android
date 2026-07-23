package com.kharcha.experiences.dashboard.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.model.TransactionType
import com.kharcha.core.domain.repository.TransactionRepository
import com.kharcha.core.common.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.kharcha.core.domain.manager.ReminderManager

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

        private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date ->
                    val startOfMonth = DateUtils.getStartOfMonth(date)
                    val endOfMonth = DateUtils.getEndOfMonth(date)
                    val startOfDay = DateUtils.getStartOfDay(date)
                    val endOfDay = DateUtils.getEndOfDay(date)

                    combine(
                        repository.getTotalByTypeAndDateRange(TransactionType.INCOME, startOfMonth, endOfMonth),
                        repository.getTotalByTypeAndDateRange(TransactionType.EXPENSE, startOfMonth, endOfMonth),
                        repository.getTransactionsByDateRange(startOfDay, endOfDay),
                        repository.getTotalBalance()
                    ) { income, expense, transactions, totalBalance ->
                        _uiState.value.copy(
                            totalIncome = income,
                            totalExpense = expense,
                            balance = totalBalance,
                            recentTransactions = transactions,
                            currentMonthLabel = DateUtils.formatMonthYear(date),
                            selectedDate = date,
                            isLoading = false
                        )
                    }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onDateSelected(date: java.time.LocalDate) {
        _uiState.update { it.copy(selectedDate = date, isLoading = true) }
    }

    fun deleteTransaction(transaction: com.kharcha.core.model.Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun scheduleReminder(context: android.content.Context, hour: Int, minute: Int, isEnabled: Boolean) {
        reminderManager.scheduleReminder(context, hour, minute, isEnabled)
    }
    
    fun sendTestNotification(context: android.content.Context) {
        reminderManager.sendTestNotification(context)
    }
}
