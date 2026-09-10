package com.kharcha.experiences.history.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.model.Transaction
import com.kharcha.core.model.TransactionType
import com.kharcha.core.domain.repository.TransactionRepository
import com.kharcha.core.datastore.KharchaPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

enum class HistoryDateMode(val label: String) {
    MONTHLY("Monthly"),
    DATE_RANGE("Date Range"),
    ALL_TIME("All Time")
}

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val dateFilteredTransactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: TransactionType? = null,
    val selectedDateMode: HistoryDateMode = HistoryDateMode.MONTHLY,
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val selectedMonthLabel: String = "",
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val kharchaPreferences: KharchaPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HistoryUiState(
            selectedMonthLabel = formatYearMonth(YearMonth.now())
        )
    )
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    init {
        viewModelScope.launch {
            kharchaPreferences.adFreeExpiry.collect { expiry ->
                _isAdFree.value = System.currentTimeMillis() < (expiry ?: 0L)
            }
        }
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                _uiState.update { state ->
                    val filtered = applyDateAndSearchFilters(
                        transactions = transactions,
                        query = state.searchQuery,
                        dateMode = state.selectedDateMode,
                        yearMonth = state.selectedYearMonth,
                        customStart = state.customStartDate,
                        customEnd = state.customEndDate
                    )
                    val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    state.copy(
                        transactions = transactions,
                        dateFilteredTransactions = filtered,
                        totalExpense = expense,
                        totalIncome = income,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.update { state ->
            val filtered = applyDateAndSearchFilters(
                transactions = state.transactions,
                query = query,
                dateMode = state.selectedDateMode,
                yearMonth = state.selectedYearMonth,
                customStart = state.customStartDate,
                customEnd = state.customEndDate
            )
            val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            state.copy(
                searchQuery = query,
                dateFilteredTransactions = filtered,
                totalExpense = expense,
                totalIncome = income
            )
        }
    }

    fun setDateMode(mode: HistoryDateMode) {
        _uiState.update { state ->
            val filtered = applyDateAndSearchFilters(
                transactions = state.transactions,
                query = state.searchQuery,
                dateMode = mode,
                yearMonth = state.selectedYearMonth,
                customStart = state.customStartDate,
                customEnd = state.customEndDate
            )
            val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            state.copy(
                selectedDateMode = mode,
                dateFilteredTransactions = filtered,
                totalExpense = expense,
                totalIncome = income
            )
        }
    }

    fun onPreviousMonth() {
        _uiState.update { state ->
            val prev = state.selectedYearMonth.minusMonths(1)
            val label = formatYearMonth(prev)
            val filtered = applyDateAndSearchFilters(
                transactions = state.transactions,
                query = state.searchQuery,
                dateMode = HistoryDateMode.MONTHLY,
                yearMonth = prev,
                customStart = state.customStartDate,
                customEnd = state.customEndDate
            )
            val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            state.copy(
                selectedDateMode = HistoryDateMode.MONTHLY,
                selectedYearMonth = prev,
                selectedMonthLabel = label,
                dateFilteredTransactions = filtered,
                totalExpense = expense,
                totalIncome = income
            )
        }
    }

    fun onNextMonth() {
        _uiState.update { state ->
            val next = state.selectedYearMonth.plusMonths(1)
            val label = formatYearMonth(next)
            val filtered = applyDateAndSearchFilters(
                transactions = state.transactions,
                query = state.searchQuery,
                dateMode = HistoryDateMode.MONTHLY,
                yearMonth = next,
                customStart = state.customStartDate,
                customEnd = state.customEndDate
            )
            val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            state.copy(
                selectedDateMode = HistoryDateMode.MONTHLY,
                selectedYearMonth = next,
                selectedMonthLabel = label,
                dateFilteredTransactions = filtered,
                totalExpense = expense,
                totalIncome = income
            )
        }
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _uiState.update { state ->
            val filtered = applyDateAndSearchFilters(
                transactions = state.transactions,
                query = state.searchQuery,
                dateMode = HistoryDateMode.DATE_RANGE,
                yearMonth = state.selectedYearMonth,
                customStart = startMillis,
                customEnd = endMillis
            )
            val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            state.copy(
                selectedDateMode = HistoryDateMode.DATE_RANGE,
                customStartDate = startMillis,
                customEndDate = endMillis,
                dateFilteredTransactions = filtered,
                totalExpense = expense,
                totalIncome = income
            )
        }
    }

    fun onTypeFilterChange(type: TransactionType?) {
        _uiState.update { state ->
            state.copy(selectedTypeFilter = type)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    private fun applyDateAndSearchFilters(
        transactions: List<Transaction>,
        query: String,
        dateMode: HistoryDateMode,
        yearMonth: YearMonth,
        customStart: Long?,
        customEnd: Long?
    ): List<Transaction> {
        var result = transactions

        // 1. Date Filter
        when (dateMode) {
            HistoryDateMode.MONTHLY -> {
                val startOfMonth = yearMonth.atDay(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val endOfMonth = yearMonth.atEndOfMonth()
                    .atTime(LocalTime.MAX)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                result = result.filter { it.date in startOfMonth..endOfMonth }
            }
            HistoryDateMode.DATE_RANGE -> {
                if (customStart != null && customEnd != null) {
                    val actualEnd = if (customEnd >= customStart) customEnd else customStart
                    result = result.filter { it.date in customStart..actualEnd }
                }
            }
            HistoryDateMode.ALL_TIME -> {
                // Keep all
            }
        }

        // 2. Search Query
        if (query.isNotBlank()) {
            result = result.filter {
                it.note.contains(query, ignoreCase = true)
                        || it.category.displayName.contains(query, ignoreCase = true)
                        || it.amount.toString().contains(query)
            }
        }

        return result
    }

    companion object {
        fun formatYearMonth(ym: YearMonth): String {
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            return ym.format(formatter)
        }
    }
}
