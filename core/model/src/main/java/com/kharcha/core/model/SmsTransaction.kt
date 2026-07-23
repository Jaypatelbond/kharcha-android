package com.kharcha.core.model

enum class SmsTransactionStatus {
    PENDING, APPROVED, REJECTED
}

data class SmsTransaction(
    val id: Long = 0,
    val sender: String,
    val body: String,
    val amount: Double,
    val type: TransactionType,
    val detectedCategory: Category,
    val detectedPaymentMode: PaymentMode,
    val bankName: String,
    val accountLast4: String = "",
    val refNumber: String = "",
    val balance: Double? = null,
    val isLoanEmi: Boolean = false,
    val timestamp: Long,
    val status: SmsTransactionStatus = SmsTransactionStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
