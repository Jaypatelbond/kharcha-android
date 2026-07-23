@file:Suppress("DEPRECATION")
package com.kharcha.tracker.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("DEPRECATION") // TODO: Migrate to Credential Manager
class GoogleDriveHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var driveService: Drive? = null

    fun initialize(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, setOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Kharcha Tracker")
            .build()
    }

    suspend fun uploadBackup(localFile: java.io.File, mimeType: String = "application/zip"): String? = withContext(Dispatchers.IO) {
        val service = driveService ?: throw IllegalStateException("Drive Service not initialized")

        val fileMetadata = File().apply {
            name = "kharcha_backup_${System.currentTimeMillis()}.zip"
            parents = listOf("appDataFolder")
        }
        val mediaContent = FileContent(mimeType, localFile)

        try {
            val file = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            file.id
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun listBackups(): List<File> = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext emptyList()
        try {
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name contains 'kharcha_backup_' and trashed = false")
                .setOrderBy("createdTime desc")
                .setFields("files(id, name, createdTime, size)")
                .execute()
            result.files
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteBackup(fileId: String): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: throw IllegalStateException("Drive Service not initialized")
        try {
            service.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun downloadBackup(fileId: String, destFile: java.io.File): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: throw IllegalStateException("Drive Service not initialized")
        try {
            val outputStream = FileOutputStream(destFile)
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
