package com.kharcha.tracker.data.mapper

import com.kharcha.tracker.data.local.entity.SmsTransactionEntity
import com.kharcha.tracker.data.local.entity.TransactionEntity
import com.kharcha.tracker.data.local.entity.TransactionWithCategory
import com.kharcha.tracker.domain.model.PaymentMode
import com.kharcha.tracker.domain.model.SmsTransaction
import com.kharcha.tracker.domain.model.SmsTransactionStatus
import com.kharcha.tracker.domain.model.Transaction
import com.kharcha.tracker.domain.model.TransactionType

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
