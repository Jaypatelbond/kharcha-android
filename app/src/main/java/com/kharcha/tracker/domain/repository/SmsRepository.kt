package com.kharcha.tracker.domain.repository

import com.kharcha.tracker.domain.model.SmsTransaction
import kotlinx.coroutines.flow.Flow

interface SmsRepository {

    suspend fun scanMessages(daysBack: Int = 90): Int

    fun getPendingTransactions(): Flow<List<SmsTransaction>>

    fun getApprovedTransactions(): Flow<List<SmsTransaction>>

    suspend fun approveTransaction(smsTransaction: SmsTransaction)

    suspend fun approveAllPending(): Int

    suspend fun rejectTransaction(smsTransaction: SmsTransaction)
}
