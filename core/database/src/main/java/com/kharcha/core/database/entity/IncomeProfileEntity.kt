package com.kharcha.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_profiles")
data class IncomeProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthlySalary: Double,
    val otherIncome: Double,        // Freelance, rent, etc.
    val monthlyExpenses: Double,    // Essential living expenses
    val lastUpdated: Long = System.currentTimeMillis()
)
