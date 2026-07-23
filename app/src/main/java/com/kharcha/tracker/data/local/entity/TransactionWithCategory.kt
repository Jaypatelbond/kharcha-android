package com.kharcha.tracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "category",
        entityColumn = "name"
    )
    val category: CategoryEntity?
)
