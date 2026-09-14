package com.kharcha.core.model

enum class BackupStorageType {
    LOCAL_ONLY,
    CLOUD_ONLY,
    SYNCED
}

data class BackupItem(
    val id: String,
    val cloudId: String? = null,
    val localFileName: String? = null,
    val name: String,
    val timestamp: Long,
    val sizeBytes: Long,
    val storageType: BackupStorageType = BackupStorageType.LOCAL_ONLY
) {
    val isCloud: Boolean get() = storageType == BackupStorageType.CLOUD_ONLY || storageType == BackupStorageType.SYNCED
    val isLocal: Boolean get() = storageType == BackupStorageType.LOCAL_ONLY || storageType == BackupStorageType.SYNCED
}
