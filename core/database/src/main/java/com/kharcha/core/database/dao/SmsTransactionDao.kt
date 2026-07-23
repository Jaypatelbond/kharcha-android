package com.kharcha.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kharcha.core.database.entity.SmsTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(smsTransaction: SmsTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(smsTransactions: List<SmsTransactionEntity>)

    @Update
    suspend fun update(smsTransaction: SmsTransactionEntity)

    @Query("SELECT * FROM sms_transactions WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingTransactions(): Flow<List<SmsTransactionEntity>>

    @Query("SELECT * FROM sms_transactions WHERE status = 'APPROVED' ORDER BY timestamp DESC")
    fun getApprovedTransactions(): Flow<List<SmsTransactionEntity>>

    @Query("SELECT * FROM sms_transactions ORDER BY timestamp DESC")
    fun getAllSmsTransactions(): Flow<List<SmsTransactionEntity>>

    @Query("UPDATE sms_transactions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM sms_transactions WHERE body = :body AND timestamp = :timestamp")
    suspend fun isDuplicate(body: String, timestamp: Long): Int

    @Query("SELECT COUNT(*) FROM sms_transactions WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM sms_transactions WHERE timestamp >= :sinceTimestamp")
    suspend fun getTransactionsSince(sinceTimestamp: Long): List<SmsTransactionEntity>
}
