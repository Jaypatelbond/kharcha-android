package com.kharcha.core.data.repository

import com.kharcha.core.database.dao.BudgetDao
import com.kharcha.core.database.entity.BudgetEntity
import com.kharcha.core.model.Budget
import com.kharcha.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun getBudgetsForMonth(month: String): Flow<List<Budget>> =
        dao.getBudgetsForMonth(month).map { list -> list.map { it.toDomain() } }

    override suspend fun getBudgetForCategoryAndMonth(categoryName: String, month: String): Budget? =
        dao.getBudgetForCategoryAndMonth(categoryName, month)?.toDomain()

    override suspend fun insertBudget(budget: Budget) {
        dao.insertBudget(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        dao.updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        dao.deleteBudget(budget.toEntity())
    }

    private fun BudgetEntity.toDomain() = Budget(
        id = id,
        categoryName = categoryName,
        amount = amount,
        month = month,
        createdAt = createdAt
    )

    private fun Budget.toEntity() = BudgetEntity(
        id = id,
        categoryName = categoryName,
        amount = amount,
        month = month,
        createdAt = createdAt
    )
}
