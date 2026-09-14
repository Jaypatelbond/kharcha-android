package com.kharcha.experiences.history.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.model.Transaction
import com.kharcha.core.model.TransactionType
import com.kharcha.core.domain.repository.TransactionRepository
import com.kharcha.core.domain.repository.CollectionRepository
import com.kharcha.core.datastore.KharchaPreferences
import com.kharcha.core.common.util.SharedFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

enum class HistoryDateMode(val label: String) {
    ALL_TIME("All Time"),
    MONTHLY("Monthly"),
    DATE_RANGE("Date Range")
}

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val dateFilteredTransactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: TransactionType? = null,
    val selectedDateMode: HistoryDateMode = HistoryDateMode.ALL_TIME, // Default ALL_TIME so all entries are visible!
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val selectedMonthLabel: String = "",
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val selectedCollection: String? = null,
    val availableCollections: List<String> = emptyList(),
    val collectionCounts: Map<String, Int> = emptyMap(),
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
        val preselected = SharedFilter.preselectedCollection
        SharedFilter.preselectedCollection = null

        viewModelScope.launch {
            kharchaPreferences.adFreeExpiry.collect { expiry ->
                _isAdFree.value = System.currentTimeMillis() < (expiry ?: 0L)
            }
        }
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                val counts = transactions
                    .groupBy { it.collection.ifBlank { "Home Expenses" } }
                    .mapValues { it.value.size }
                val cols = counts.keys.toList().sorted()

                _uiState.update { state ->
                    val initialCol = preselected ?: state.selectedCollection
                    val filtered = applyDateAndSearchFilters(
                        transactions = transactions,
                        query = state.searchQuery,
                        dateMode = state.selectedDateMode,
                        yearMonth = state.selectedYearMonth,
                        customStart = state.customStartDate,
                        customEnd = state.customEndDate,
                        selectedCollection = initialCol
                    )
                    val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    state.copy(
                        transactions = transactions,
                        dateFilteredTransactions = filtered,
                        totalExpense = expense,
                        totalIncome = income,
                        selectedCollection = initialCol,
                        availableCollections = cols,
                        collectionCounts = counts,
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
                customEnd = state.customEndDate,
                selectedCollection = state.selectedCollection
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
                customEnd = state.customEndDate,
                selectedCollection = state.selectedCollection
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
                customEnd = state.customEndDate,
                selectedCollection = state.selectedCollection
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
                customEnd = state.customEndDate,
                selectedCollection = state.selectedCollection
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
                customEnd = endMillis,
                selectedCollection = state.selectedCollection
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

    fun onSelectCollection(collection: String?) {
        _uiState.update { state ->
            val targetCollection = if (collection == "All" || collection == "All Collections" || collection == "All Books") null else collection

            // When a collection is selected, if current mode is MONTHLY and that month has 0 entries for this collection,
            // automatically switch to ALL_TIME so all entries are immediately shown!
            val newMode = if (targetCollection != null) {
                if (state.selectedDateMode == HistoryDateMode.MONTHLY) {
                    val hasInMonth = state.transactions.any {
                        val colName = it.collection.ifBlank { "Home Expenses" }
                        colName.equals(targetCollection, ignoreCase = true) && isInYearMonth(it.date, state.selectedYearMonth)
                    }
                    if (!hasInMonth) HistoryDateMode.ALL_TIME else state.selectedDateMode
                } else {
                    state.selectedDateMode
                }
            } else {
                state.selectedDateMode
            }

            val filtered = applyDateAndSearchFilters(
                transactions = state.transactions,
                query = state.searchQuery,
                dateMode = newMode,
                yearMonth = state.selectedYearMonth,
                customStart = state.customStartDate,
                customEnd = state.customEndDate,
                selectedCollection = targetCollection
            )
            val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            state.copy(
                selectedCollection = targetCollection,
                selectedDateMode = newMode,
                dateFilteredTransactions = filtered,
                totalExpense = expense,
                totalIncome = income
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    private fun isInYearMonth(epochMillis: Long, ym: YearMonth): Boolean {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.year == ym.year && date.month == ym.month
    }

    private fun applyDateAndSearchFilters(
        transactions: List<Transaction>,
        query: String,
        dateMode: HistoryDateMode,
        yearMonth: YearMonth,
        customStart: Long?,
        customEnd: Long?,
        selectedCollection: String? = null
    ): List<Transaction> {
        var result = transactions

        // 0. Collection Filter (handles blank collections as "Home Expenses")
        if (!selectedCollection.isNullOrBlank() && selectedCollection != "All" && selectedCollection != "All Collections" && selectedCollection != "All Books") {
            result = result.filter {
                val colName = it.collection.ifBlank { "Home Expenses" }
                colName.equals(selectedCollection, ignoreCase = true)
            }
        }

        // 1. Date Filter
        when (dateMode) {
            HistoryDateMode.MONTHLY -> {
                runCatching {
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
            return runCatching {
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                ym.format(formatter)
            }.getOrDefault(ym.toString())
        }
    }
}
