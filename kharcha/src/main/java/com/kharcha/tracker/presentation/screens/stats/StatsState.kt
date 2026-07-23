package com.kharcha.tracker.presentation.screens.stats

import com.kharcha.core.domain.repository.CategoryTotal

enum class TimeRange(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year"),
    FINANCIAL_YEAR("FY")
}

data class StatsData(
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val dailyTotals: Map<Long, Double> = emptyMap(),
    val isLoading: Boolean = false
)

data class StatsUiState(
    val selectedRange: TimeRange = TimeRange.MONTH,
    val stats: Map<TimeRange, StatsData> = TimeRange.entries.associateWith { StatsData() }
)
