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
    type = TransactionType.valueOf(transaction.type),
    category = category?.toDomain() ?: getFallbackCategory(transaction.category, transaction.type),
    paymentMode = PaymentMode.valueOf(transaction.paymentMode),
    note = transaction.note,
    date = transaction.date,
    createdAt = transaction.createdAt
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category.name,
    paymentMode = paymentMode.name,
    note = note,
    date = date,
    createdAt = createdAt
)

fun SmsTransactionEntity.toDomain() = SmsTransaction(
    id = id,
    sender = sender,
    body = body,
    amount = amount,
    type = TransactionType.valueOf(type),
    detectedCategory = getFallbackCategory(detectedCategory, type),
    detectedPaymentMode = PaymentMode.valueOf(detectedPaymentMode),
    bankName = bankName,
    accountLast4 = accountLast4,
    refNumber = refNumber,
    balance = balance,
    timestamp = timestamp,
    status = SmsTransactionStatus.valueOf(status),
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
