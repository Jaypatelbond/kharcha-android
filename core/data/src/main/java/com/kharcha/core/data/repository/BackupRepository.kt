package com.kharcha.core.data.repository

import android.content.Context
import com.kharcha.core.database.AppDatabase
import com.kharcha.core.data.remote.GoogleDriveHelper
import com.kharcha.core.model.BackupItem
import com.kharcha.core.model.BackupStorageType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveHelper: GoogleDriveHelper,
    private val database: AppDatabase
) {

    private val backupMutex = Mutex()

    private val localBackupDir: File
        get() = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }

    private fun getDbFilesToZip(): List<File> {
        try {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dbName = "kharcha_db"
        val dbPath = context.getDatabasePath(dbName)
        val dbShm = File(dbPath.parent, "$dbName-shm")
        val dbWal = File(dbPath.parent, "$dbName-wal")

        return listOfNotNull(
            if (dbPath.exists()) dbPath else null,
            if (dbShm.exists()) dbShm else null,
            if (dbWal.exists()) dbWal else null
        )
    }

    suspend fun performLocalBackup(): BackupItem = withContext(Dispatchers.IO) {
        backupMutex.withLock {
            val timestamp = System.currentTimeMillis()
            val backupFile = File(localBackupDir, "kharcha_backup_$timestamp.zip")
            zipFiles(getDbFilesToZip(), backupFile)
            BackupItem(
                id = backupFile.name,
                localFileName = backupFile.name,
                name = backupFile.name,
                timestamp = timestamp,
                sizeBytes = backupFile.length(),
                storageType = BackupStorageType.LOCAL_ONLY
            )
        }
    }

    suspend fun performCloudBackup() = withContext(Dispatchers.IO) {
        backupMutex.withLock {
            val timestamp = System.currentTimeMillis()
            val localBackupFile = File(localBackupDir, "kharcha_backup_$timestamp.zip")
            zipFiles(getDbFilesToZip(), localBackupFile)

            val fileId = driveHelper.uploadBackup(localBackupFile)
            if (fileId == null) throw Exception("Upload failed: No file ID returned from Google Drive")
        }
    }

    suspend fun ensureInitialBackup(): BackupItem? = withContext(Dispatchers.IO) {
        backupMutex.withLock {
            val existing = getLocalBackupsInternal()
            if (existing.isEmpty()) {
                val timestamp = System.currentTimeMillis()
                val backupFile = File(localBackupDir, "kharcha_backup_$timestamp.zip")
                zipFiles(getDbFilesToZip(), backupFile)
                BackupItem(
                    id = backupFile.name,
                    localFileName = backupFile.name,
                    name = backupFile.name,
                    timestamp = timestamp,
                    sizeBytes = backupFile.length(),
                    storageType = BackupStorageType.LOCAL_ONLY
                )
            } else {
                existing.firstOrNull()
            }
        }
    }

    private fun getLocalBackupsInternal(): List<BackupItem> {
        val files = localBackupDir.listFiles { _, name -> name.startsWith("kharcha_backup_") && name.endsWith(".zip") }
            ?: return emptyList()

        return files.map { file ->
            val timestamp = file.name
                .removePrefix("kharcha_backup_")
                .removeSuffix(".zip")
                .toLongOrNull() ?: file.lastModified()

            BackupItem(
                id = file.name,
                localFileName = file.name,
                name = file.name,
                timestamp = timestamp,
                sizeBytes = file.length(),
                storageType = BackupStorageType.LOCAL_ONLY
            )
        }.sortedByDescending { it.timestamp }
    }

    suspend fun getLocalBackups(): List<BackupItem> = withContext(Dispatchers.IO) {
        getLocalBackupsInternal()
    }

    suspend fun getCloudBackups(): List<BackupItem> = withContext(Dispatchers.IO) {
        val driveFiles = driveHelper.listBackups()
        driveFiles.map { file ->
            val timestamp = file.createdTime?.value ?: System.currentTimeMillis()
            BackupItem(
                id = file.id,
                cloudId = file.id,
                name = file.name ?: "kharcha_backup_$timestamp.zip",
                timestamp = timestamp,
                sizeBytes = file.getSize() ?: 0L,
                storageType = BackupStorageType.CLOUD_ONLY
            )
        }
    }

    suspend fun getAllBackups(includeCloud: Boolean): List<BackupItem> = withContext(Dispatchers.IO) {
        ensureInitialBackup()
        val localItems = getLocalBackupsInternal()

        if (!includeCloud) {
            return@withContext localItems.distinctBy { it.name }.sortedByDescending { it.timestamp }
        }

        val cloudItems = try {
            getCloudBackups()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        // Merge and deduplicate matching local and cloud backups
        val merged = mutableListOf<BackupItem>()
        val matchedCloudIds = mutableSetOf<String>()

        for (local in localItems) {
            val matchingCloud = cloudItems.find { cloud ->
                cloud.name == local.name || abs(cloud.timestamp - local.timestamp) < 60_000L
            }

            if (matchingCloud != null) {
                matchedCloudIds.add(matchingCloud.id)
                merged.add(
                    BackupItem(
                        id = local.id,
                        cloudId = matchingCloud.id,
                        localFileName = local.id,
                        name = local.name,
                        timestamp = local.timestamp,
                        sizeBytes = if (local.sizeBytes > 0) local.sizeBytes else matchingCloud.sizeBytes,
                        storageType = BackupStorageType.SYNCED
                    )
                )
            } else {
                merged.add(local)
            }
        }

        // Add remaining cloud-only backups
        for (cloud in cloudItems) {
            if (cloud.id !in matchedCloudIds) {
                merged.add(cloud)
            }
        }

        // Final deduplication by name and timestamp
        merged.distinctBy { it.name }
            .sortedByDescending { it.timestamp }
    }

    suspend fun deleteBackup(item: BackupItem): Boolean = withContext(Dispatchers.IO) {
        var success = true
        val cloudId = item.cloudId
        if (cloudId != null) {
            try {
                driveHelper.deleteBackup(cloudId)
            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            }
        }
        val localFileName = item.localFileName
        if (localFileName != null) {
            val file = File(localBackupDir, localFileName)
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) success = false
            }
        }
        success
    }

    suspend fun restore(item: BackupItem) = withContext(Dispatchers.IO) {
        val localFileName = item.localFileName
        if (localFileName != null) {
            val file = File(localBackupDir, localFileName)
            if (file.exists()) {
                unzipAndReplaceDb(file)
                return@withContext
            }
        }

        val cloudId = item.cloudId
        if (cloudId != null) {
            val zipFile = File(context.cacheDir, "restore.zip")
            if (driveHelper.downloadBackup(cloudId, zipFile)) {
                unzipAndReplaceDb(zipFile)
                zipFile.delete()
            } else {
                throw Exception("Download from Google Drive failed")
            }
        } else {
            throw Exception("Backup file not found on device or cloud")
        }
    }

    private fun zipFiles(files: List<File>, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            files.forEach { file ->
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
    }

    private fun unzipAndReplaceDb(zipFile: File) {
        database.close()
        val dbPath = context.getDatabasePath("kharcha_db").parentFile

        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(dbPath, entry.name)
                FileOutputStream(outFile).use { fos ->
                    zis.copyTo(fos)
                }
                entry = zis.nextEntry
            }
        }
    }
}
