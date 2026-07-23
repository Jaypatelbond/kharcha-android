package com.kharcha.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_transactions")
data class SmsTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val amount: Double,
    val type: String,
    val detectedCategory: String,
    val detectedPaymentMode: String,
    val bankName: String,
    val accountLast4: String = "",
    val refNumber: String = "",
    val balance: Double? = null,
    val timestamp: Long,
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)
