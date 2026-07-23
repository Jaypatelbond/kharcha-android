package com.kharcha.tracker.domain.model

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val paymentMode: PaymentMode,
    val note: String = "",
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
