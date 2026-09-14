package com.kharcha.core.data.mapper

import com.kharcha.core.database.entity.TransactionEntity
import com.kharcha.core.database.entity.TransactionWithCategory
import com.kharcha.core.model.PaymentMode
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
