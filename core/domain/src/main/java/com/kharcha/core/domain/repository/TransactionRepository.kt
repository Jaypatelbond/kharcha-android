package com.kharcha.core.domain.repository

import com.kharcha.core.model.Category
import com.kharcha.core.model.Transaction
import com.kharcha.core.model.TransactionType
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
    fun getAllCollections(): Flow<List<String>>
    fun getTransactionsByCollection(collection: String): Flow<List<Transaction>>
}
