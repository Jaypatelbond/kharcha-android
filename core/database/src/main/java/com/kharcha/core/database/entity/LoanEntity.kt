package com.kharcha.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g., "Home Loan"
    val bankName: String,
    val principalAmount: Double,
    val interestRate: Double, // Annual interest rate in %
    val tenureMonths: Int,
    val startDate: Long,
    val emiAmount: Double,
    val outstandingBalance: Double,
    val status: String, // ACTIVE, CLOSED
    val type: String // HOME, CAR, PERSONAL, EDUCATION, OTHER
)
