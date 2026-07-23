package com.kharcha.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long, // References CategoryEntity.id
    val note: String,
    val type: String, // "INCOME" or "EXPENSE"
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val startDate: Long,
    val endDate: Long? = null, // Optional end date
    val lastProcessedDate: Long? = null,
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = false,
    val nextDueDate: Long? = null
)
