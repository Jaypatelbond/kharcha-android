package com.kharcha.experiences.dashboard.impl

import com.kharcha.core.model.Transaction

data class DashboardUiState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val currentMonthLabel: String = "",
    val selectedDate: java.time.LocalDate = java.time.LocalDate.now(),
    val isLoading: Boolean = true
)
