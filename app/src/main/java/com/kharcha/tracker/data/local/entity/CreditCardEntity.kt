package com.kharcha.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardName: String,           // e.g., "HDFC Regalia"
    val bankName: String,
    val creditLimit: Double,
    val outstandingBalance: Double,
    val apr: Double,                // Annual interest rate % (e.g., 42.0)
    val minPaymentPercent: Double,  // e.g., 5.0
    val billingDate: Int,           // Day of month (1–28)
    val dueDate: Int,               // Day of month
    val statementBalance: Double,
    val status: String              // ACTIVE, CLOSED
)
