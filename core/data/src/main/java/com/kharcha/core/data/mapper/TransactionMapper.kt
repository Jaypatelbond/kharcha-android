package com.kharcha.core.data.mapper

import com.kharcha.core.database.entity.SmsTransactionEntity
import com.kharcha.core.database.entity.TransactionEntity
import com.kharcha.core.database.entity.TransactionWithCategory
import com.kharcha.core.model.PaymentMode
import com.kharcha.core.model.SmsTransaction
import com.kharcha.core.model.SmsTransactionStatus
import com.kharcha.core.model.Transaction
import com.kharcha.core.model.TransactionType

fun TransactionWithCategory.toDomain() = Transaction(
    id = transaction.id,
    amount = transaction.amount,
    type = runCatching { TransactionType.valueOf(transaction.type) }.getOrDefault(TransactionType.EXPENSE),
    category = category?.toDomain() ?: getFallbackCategory(transaction.category, transaction.type),
    paymentMode = runCatching { PaymentMode.valueOf(transaction.paymentMode) }.getOrElse {
        PaymentMode.entries.find { it.name.equals(transaction.paymentMode, ignoreCase = true) } ?: PaymentMode.OTHER
    },
    note = transaction.note,
    date = transaction.date,
    createdAt = transaction.createdAt,
    collection = transaction.collection
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category.name,
    paymentMode = paymentMode.name,
    note = note,
    date = date,
    createdAt = createdAt,
    collection = collection
)

fun SmsTransactionEntity.toDomain() = SmsTransaction(
    id = id,
    sender = sender,
    body = body,
    amount = amount,
    type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
    detectedCategory = getFallbackCategory(detectedCategory, type),
    detectedPaymentMode = runCatching { PaymentMode.valueOf(detectedPaymentMode) }.getOrElse {
        PaymentMode.entries.find { it.name.equals(detectedPaymentMode, ignoreCase = true) } ?: PaymentMode.OTHER
    },
    bankName = bankName,
    accountLast4 = accountLast4,
    refNumber = refNumber,
    balance = balance,
    timestamp = timestamp,
    status = runCatching { SmsTransactionStatus.valueOf(status) }.getOrDefault(SmsTransactionStatus.PENDING),
    createdAt = createdAt
)

fun SmsTransaction.toEntity() = SmsTransactionEntity(
    id = id,
    sender = sender,
    body = body,
    amount = amount,
    type = type.name,
    detectedCategory = detectedCategory.name,
    detectedPaymentMode = detectedPaymentMode.name,
    bankName = bankName,
    accountLast4 = accountLast4,
    refNumber = refNumber,
    balance = balance,
    timestamp = timestamp,
    status = status.name,
    createdAt = createdAt
)
