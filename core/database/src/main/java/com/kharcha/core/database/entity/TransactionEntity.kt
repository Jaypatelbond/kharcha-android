package com.kharcha.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String,
    val category: String,
    val paymentMode: String,
    val note: String = "",
    val date: Long,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "collection", defaultValue = "Home Expenses")
    val collection: String = "Home Expenses"
)
