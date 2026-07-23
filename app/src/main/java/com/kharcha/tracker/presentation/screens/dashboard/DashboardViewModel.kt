package com.kharcha.tracker.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.tracker.domain.model.TransactionType
import com.kharcha.tracker.domain.repository.TransactionRepository
import com.kharcha.tracker.util.DateUtils
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

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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

    fun deleteTransaction(transaction: com.kharcha.tracker.domain.model.Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun scheduleReminder(context: android.content.Context, hour: Int, minute: Int, isEnabled: Boolean) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        if (isEnabled) {
            val now = java.time.LocalDateTime.now()
            var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (target.isBefore(now)) {
                target = target.plusDays(1)
            }
            val delay = java.time.Duration.between(now, target).toMillis()

            val reminderRequest = androidx.work.PeriodicWorkRequestBuilder<com.kharcha.tracker.worker.ReminderWorker>(
                24, java.util.concurrent.TimeUnit.HOURS
            )
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .addTag("daily_reminder")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "daily_reminder",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                reminderRequest
            )
        } else {
            workManager.cancelUniqueWork("daily_reminder")
        }
    }
    fun sendTestNotification(context: android.content.Context) {
        com.kharcha.tracker.util.NotificationHelper.showReminderNotification(
            context,
            title = "Test Reminder \uD83D\uDD14",
            message = "This is how your daily reminder will look!"
        )
    }
}
