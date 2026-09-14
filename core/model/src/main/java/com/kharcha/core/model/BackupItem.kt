package com.kharcha.core.model

data class BackupItem(
    val id: String,
    val name: String,
    val timestamp: Long,
    val sizeBytes: Long,
    val isCloud: Boolean
)
