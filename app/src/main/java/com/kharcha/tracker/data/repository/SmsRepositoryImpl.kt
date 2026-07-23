package com.kharcha.tracker.data.repository

import com.kharcha.tracker.data.local.dao.SmsTransactionDao
import com.kharcha.tracker.data.mapper.toDomain
import com.kharcha.tracker.data.mapper.toEntity
import com.kharcha.tracker.domain.model.SmsTransaction
import com.kharcha.tracker.domain.repository.SmsRepository
import com.kharcha.tracker.domain.repository.TransactionRepository
import com.kharcha.tracker.util.SmsReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    private val smsDao: SmsTransactionDao,
    private val transactionRepository: TransactionRepository,
    private val smsReader: SmsReader
) : SmsRepository {

    override suspend fun scanMessages(daysBack: Int): Int {
        val messages = smsReader.readSms(daysBack)
        var newCount = 0
        
        val sinceTimestamp = System.currentTimeMillis() - (daysBack * 24L * 60 * 60 * 1000)
        val existingTransactions = smsDao.getTransactionsSince(sinceTimestamp)
        val existingSet = existingTransactions.mapTo(HashSet()) { "${it.body}|${it.timestamp}" }
        val newTransactions = mutableListOf<com.kharcha.tracker.data.local.entity.SmsTransactionEntity>()
        
        messages.forEach { msg ->
            val signature = "${msg.body}|${msg.timestamp}"
            if (!existingSet.contains(signature)) {
                newTransactions.add(msg.toEntity())
                existingSet.add(signature)
                newCount++
            }
        }
        
        if (newTransactions.isNotEmpty()) {
            smsDao.insertAll(newTransactions)
        }
        return newCount
    }

    override fun getPendingTransactions(): Flow<List<SmsTransaction>> =
        smsDao.getPendingTransactions().map { entities -> entities.map { it.toDomain() } }

    override fun getApprovedTransactions(): Flow<List<SmsTransaction>> =
        smsDao.getApprovedTransactions().map { entities -> entities.map { it.toDomain() } }

    override suspend fun approveTransaction(smsTransaction: SmsTransaction) {
        val transaction = com.kharcha.tracker.domain.model.Transaction(
            amount = smsTransaction.amount,
            type = smsTransaction.type,
            category = smsTransaction.detectedCategory,
            paymentMode = smsTransaction.detectedPaymentMode,
            note = "${smsTransaction.bankName} • ${smsTransaction.body.take(40)}...",
            date = smsTransaction.timestamp
        )
        transactionRepository.insertTransaction(transaction)
        smsDao.updateStatus(smsTransaction.id, "APPROVED")
    }

    override suspend fun approveAllPending(): Int {
        val pending = smsDao.getPendingTransactions().first()
        var count = 0
        pending.forEach { entity ->
            val sms = entity.toDomain()
            val transaction = com.kharcha.tracker.domain.model.Transaction(
                amount = sms.amount,
                type = sms.type,
                category = sms.detectedCategory,
                paymentMode = sms.detectedPaymentMode,
                note = "${sms.bankName} • ${sms.body.take(40)}...",
                date = sms.timestamp
            )
            transactionRepository.insertTransaction(transaction)
            smsDao.updateStatus(entity.id, "APPROVED")
            count++
        }
        return count
    }

    override suspend fun rejectTransaction(smsTransaction: SmsTransaction) {
        smsDao.updateStatus(smsTransaction.id, "REJECTED")
    }
}
