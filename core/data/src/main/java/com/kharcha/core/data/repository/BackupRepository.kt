package com.kharcha.core.data.repository

import android.content.Context
import com.kharcha.core.database.AppDatabase
import com.kharcha.core.data.remote.GoogleDriveHelper
import com.kharcha.core.model.BackupItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveHelper: GoogleDriveHelper,
    private val database: AppDatabase
) {

    private val localBackupDir: File
        get() = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }

    private fun getDbFilesToZip(): List<File> {
        // Checkpoint WAL to ensure data is written to main DB file
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
        val timestamp = System.currentTimeMillis()
        val backupFile = File(localBackupDir, "kharcha_backup_$timestamp.zip")
        zipFiles(getDbFilesToZip(), backupFile)
        BackupItem(
            id = backupFile.name,
            name = backupFile.name,
            timestamp = timestamp,
            sizeBytes = backupFile.length(),
            isCloud = false
        )
    }

    suspend fun performCloudBackup() = withContext(Dispatchers.IO) {
        // Also ensure local backup is preserved
        val timestamp = System.currentTimeMillis()
        val localBackupFile = File(localBackupDir, "kharcha_backup_$timestamp.zip")
        zipFiles(getDbFilesToZip(), localBackupFile)

        // Upload to Drive
        val fileId = driveHelper.uploadBackup(localBackupFile)
        if (fileId == null) throw Exception("Upload failed: No file ID returned")
    }

    suspend fun ensureInitialBackup(): BackupItem? = withContext(Dispatchers.IO) {
        val existing = getLocalBackupsInternal()
        if (existing.isEmpty()) {
            performLocalBackup()
        } else {
            existing.firstOrNull()
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
                name = file.name,
                timestamp = timestamp,
                sizeBytes = file.length(),
                isCloud = false
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
                name = file.name ?: "kharcha_backup_$timestamp.zip",
                timestamp = timestamp,
                sizeBytes = file.getSize() ?: 0L,
                isCloud = true
            )
        }
    }

    suspend fun getAllBackups(includeCloud: Boolean): List<BackupItem> = withContext(Dispatchers.IO) {
        ensureInitialBackup()
        val local = getLocalBackupsInternal()
        if (!includeCloud) return@withContext local

        val cloud = try {
            getCloudBackups()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        (local + cloud).sortedByDescending { it.timestamp }
    }

    suspend fun deleteBackup(item: BackupItem): Boolean = withContext(Dispatchers.IO) {
        if (item.isCloud) {
            driveHelper.deleteBackup(item.id)
        } else {
            val file = File(localBackupDir, item.id)
            if (file.exists()) file.delete() else true
        }
    }

    suspend fun restore(item: BackupItem) = withContext(Dispatchers.IO) {
        if (item.isCloud) {
            val zipFile = File(context.cacheDir, "restore.zip")
            if (driveHelper.downloadBackup(item.id, zipFile)) {
                unzipAndReplaceDb(zipFile)
                zipFile.delete()
            } else {
                throw Exception("Download from Google Drive failed")
            }
        } else {
            val file = File(localBackupDir, item.id)
            if (!file.exists()) throw Exception("Local backup file not found")
            unzipAndReplaceDb(file)
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
