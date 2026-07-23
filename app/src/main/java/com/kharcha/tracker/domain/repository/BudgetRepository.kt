package com.kharcha.tracker.domain.repository

import com.kharcha.tracker.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetsForMonth(month: String): Flow<List<Budget>>
    suspend fun getBudgetForCategoryAndMonth(categoryName: String, month: String): Budget?
    suspend fun insertBudget(budget: Budget)
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
}
