package com.kharcha.tracker.data.repository

import com.kharcha.tracker.data.local.dao.TransactionDao
import com.kharcha.tracker.data.mapper.getFallbackCategory
import com.kharcha.tracker.data.mapper.toDomain
import com.kharcha.tracker.data.mapper.toEntity
import com.kharcha.tracker.domain.model.Transaction
import com.kharcha.tracker.domain.model.TransactionType
import com.kharcha.tracker.domain.repository.CategoryTotal
import com.kharcha.tracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        dao.getAllTransactions().map { entities -> entities.map { it.toDomain() } }

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        dao.getTransactionsByDateRange(startDate, endDate).map { entities -> entities.map { it.toDomain() } }

    override fun searchTransactions(query: String): Flow<List<Transaction>> =
        dao.searchTransactions(query).map { entities -> entities.map { it.toDomain() } }

    override fun getTotalByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Flow<Double> =
        dao.getTotalByTypeAndDateRange(type.name, startDate, endDate)

    override fun getCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>> =
        dao.getCategoryTotals(startDate, endDate).map { raw ->
            raw.map { item ->
                // Mapped Category or Fallback
                val category = item.category?.toDomain() ?: getFallbackCategory(
                    name = item.categoryName ?: "Others",
                    type = "EXPENSE" // Query filters by EXPENSE
                )
                CategoryTotal(
                    category = category,
                    total = item.total
                )
            }
        }

    override fun getDailyTotals(startDate: Long, endDate: Long): Flow<Map<Long, Double>> =
        dao.getDailyTotals(startDate, endDate).map { raw ->
            raw.associate { it.dayTimestamp to it.total }
        }

    override suspend fun insertTransaction(transaction: Transaction) =
        dao.insert(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        dao.update(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        dao.delete(transaction.toEntity())

    override suspend fun getTransactionById(id: Long): Transaction? =
        dao.getById(id)?.toDomain()

    override fun getTotalBalance(): Flow<Double> =
        dao.getTotalBalance()
}
