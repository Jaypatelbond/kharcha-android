package com.kharcha.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryName", "month"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryName: String, // "All" or category name
    val amount: Double,
    val month: String, // "YYYY-MM"
    val createdAt: Long = System.currentTimeMillis()
)
