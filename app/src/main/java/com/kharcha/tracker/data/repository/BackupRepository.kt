package com.kharcha.tracker.data.repository

import android.content.Context
import com.kharcha.tracker.data.local.AppDatabase
import com.kharcha.tracker.data.remote.GoogleDriveHelper
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

    suspend fun performCloudBackup() = withContext(Dispatchers.IO) {
        // 1. Checkpoint WAL to ensure data is in main DB file
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

        // 2. Zip the database files
        val dbName = "kharcha_db"
        val dbPath = context.getDatabasePath(dbName)
        val dbShm = File(dbPath.parent, "$dbName-shm")
        val dbWal = File(dbPath.parent, "$dbName-wal")

        val filesToZip = listOfNotNull(
            if (dbPath.exists()) dbPath else null,
            if (dbShm.exists()) dbShm else null,
            if (dbWal.exists()) dbWal else null
        )

        val zipFile = File(context.cacheDir, "backup.zip")
        zipFiles(filesToZip, zipFile)

        // 3. Upload to Drive
        val fileId = driveHelper.uploadBackup(zipFile)
        zipFile.delete()
        
        if (fileId == null) throw Exception("Upload failed: No file ID returned")
    }

    suspend fun getCloudBackups() = driveHelper.listBackups()

    suspend fun deleteCloudBackup(fileId: String) = driveHelper.deleteBackup(fileId)

    suspend fun restoreFromCloud(fileId: String) = withContext(Dispatchers.IO) {
        val zipFile = File(context.cacheDir, "restore.zip")
        if (driveHelper.downloadBackup(fileId, zipFile)) {
            // Unzip and restore
            unzipAndReplaceDb(zipFile)
            zipFile.delete()
        } else {
             throw Exception("Download failed")
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
        // We need to close the DB connection before replacing files to avoid lock issues.
        // However, Room is singleton. This is tricky.
        // Best approach: Close DB, overwrite, then crash/restart app or re-open.
        // For this simple implementation, we will attempt to overwrite. The user must restart the app data reload.
        
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
