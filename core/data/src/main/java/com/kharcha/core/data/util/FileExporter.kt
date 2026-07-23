package com.kharcha.core.data.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.OutputStream

object FileExporter {

    fun saveToDownloads(context: Context, filename: String, mimeType: String, writeBlock: (OutputStream) -> Unit): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            
            return uri?.also {
                resolver.openOutputStream(it)?.use { outputStream ->
                    writeBlock(outputStream)
                }
            }
        } else {
            // Legacy approach: specific external directory
            // Note: This requires WRITE_EXTERNAL_STORAGE permission on Android < 10 if not app-specific
            // Check if we have permission or fall back to app-specific
            
            // For simplicity and to avoid crashing, we'll try public Downloads first, 
            // but if it fails (permission), we might need to catch it. 
            // However, since we haven't asked for permission, this will fail on API < 29 
            // unless we use getExternalFilesDir (which is what we had).
            
            // User likely issues are on modern Android. Let's try to target public Downloads 
            // but fallback to app-specific if needed.
            
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, filename)
                file.outputStream().use { writeBlock(it) }
                return Uri.fromFile(file)
            } catch (e: Exception) {
                // Fallback to app-specific if permission denied
                val appDir = File(context.getExternalFilesDir(null), "exports")
                if (!appDir.exists()) appDir.mkdirs()
                val file = File(appDir, filename)
                file.outputStream().use { writeBlock(it) }
                return Uri.fromFile(file)
            }
        }
    }
}
