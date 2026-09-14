package com.kharcha.experiences.dashboard.impl

import com.kharcha.core.model.Transaction
import java.time.LocalDate

data class CollectionStat(
    val name: String,
    val totalExpense: Double,
    val count: Int,
    val isDefault: Boolean = false
)

data class DashboardUiState(
    val totalKharchaAllTime: Double = 0.0,
    val monthlyKharcha: Double = 0.0,
    val dailyKharcha: Double = 0.0,
    val totalTransactionsCount: Int = 0,
    val collections: List<CollectionStat> = emptyList(),
    val selectedCollection: String? = null,
    val selectedCollectionTotal: Double? = null,
    val currentMonthLabel: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val displayedTransactions: List<Transaction> = emptyList(),
    val isFilteringByCollection: Boolean = false,
    val isLoading: Boolean = true
)
