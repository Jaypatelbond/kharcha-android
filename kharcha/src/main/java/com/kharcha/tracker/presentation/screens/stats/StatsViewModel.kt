package com.kharcha.tracker.presentation.screens.stats

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        // Load independent data for all ranges initially or just the default?
        // Let's load current default first, others will load on demand or we can load all.
        // For a smooth UI, preloading is nice, but might be heavy. 
        // Let's just load the initial one.
        loadStats(TimeRange.MONTH)
    }

    fun onTimeRangeChange(range: TimeRange) {
        _uiState.update { it.copy(selectedRange = range) }
        // Check if we already have data for this range? 
        // Or always refresh? Let's refresh to ensure up-to-date data.
        loadStats(range)
    }

    private fun loadStats(range: TimeRange) {
        val (start, end) = getDateRange(range)

        // Set loading for specific range
        _uiState.update { currentState ->
            val currentStats = currentState.stats[range] ?: StatsData()
            currentState.copy(
                stats = currentState.stats + (range to currentStats.copy(isLoading = true))
            )
        }

        viewModelScope.launch {
            combine(
                repository.getCategoryTotals(start, end),
                repository.getTotalByTypeAndDateRange(TransactionType.EXPENSE, start, end),
                repository.getTotalByTypeAndDateRange(TransactionType.INCOME, start, end),
                repository.getDailyTotals(start, end)
            ) { categories, expense, income, daily ->
                StatsData(
                    categoryTotals = categories,
                    totalExpense = expense,
                    totalIncome = income,
                    dailyTotals = daily,
                    isLoading = false
                )
            }.collect { newData ->
                _uiState.update { currentState ->
                    currentState.copy(
                        stats = currentState.stats + (range to newData)
                    )
                }
            }
        }
    }

    private fun getDateRange(range: TimeRange): Pair<Long, Long> = when (range) {
        TimeRange.WEEK -> DateUtils.getStartOfWeek() to DateUtils.getEndOfDay()
        TimeRange.MONTH -> DateUtils.getStartOfMonth() to DateUtils.getEndOfMonth()
        TimeRange.YEAR -> DateUtils.getStartOfYear() to DateUtils.getEndOfDay()
        TimeRange.FINANCIAL_YEAR -> DateUtils.getFinancialYearStart() to DateUtils.getFinancialYearEnd()
    }
}

