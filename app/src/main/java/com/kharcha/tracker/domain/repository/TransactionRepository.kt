package com.kharcha.tracker.domain.repository

import com.kharcha.tracker.domain.model.Category
import com.kharcha.tracker.domain.model.Transaction
import com.kharcha.tracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(
    val category: Category,
    val total: Double
)

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun getTotalByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Flow<Double>
    fun getCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>
    fun getDailyTotals(startDate: Long, endDate: Long): Flow<Map<Long, Double>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: Long): Transaction?
    fun getTotalBalance(): Flow<Double>
}
