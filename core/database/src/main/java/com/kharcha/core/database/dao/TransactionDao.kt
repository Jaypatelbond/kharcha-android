package com.kharcha.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kharcha.core.database.entity.CategoryEntity
import com.kharcha.core.database.entity.TransactionEntity
import com.kharcha.core.database.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

data class CategoryTotalWithEntity(
    @Embedded val category: CategoryEntity?,
    val categoryName: String?, // Fallback if category entity is null
    val total: Double
)

data class DailyTotalRaw(
    val dayTimestamp: Long,
    val total: Double
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionWithCategory?

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionWithCategory>>

    @androidx.room.Transaction
    @Query("""
        SELECT * FROM transactions 
        WHERE note LIKE '%' || :query || '%' 
        OR category LIKE '%' || :query || '%' 
        ORDER BY date DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = :type AND date BETWEEN :start AND :end")
    fun getTotalByTypeAndDateRange(type: String, start: Long, end: Long): Flow<Double>

    @Query("""
        SELECT C.*, T.category as categoryName, SUM(T.amount) as total 
        FROM transactions T
        LEFT JOIN categories C ON T.category = C.name AND C.type = 'EXPENSE'
        WHERE T.type = 'EXPENSE' AND T.date BETWEEN :start AND :end 
        GROUP BY T.category 
        ORDER BY total DESC
    """)
    fun getCategoryTotals(start: Long, end: Long): Flow<List<CategoryTotalWithEntity>>

    @Query("""
        SELECT (date / 86400000) * 86400000 as dayTimestamp, SUM(amount) as total 
        FROM transactions 
        WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end 
        GROUP BY dayTimestamp 
        ORDER BY dayTimestamp ASC
    """)
    fun getDailyTotals(start: Long, end: Long): Flow<List<DailyTotalRaw>>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0.0) 
        FROM transactions
    """)
    fun getTotalBalance(): Flow<Double>
}
