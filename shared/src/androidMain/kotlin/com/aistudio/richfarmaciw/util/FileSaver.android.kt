package com.aistudio.richfarmaciw.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object AndroidContextHolder {
    var appContext: Context? = null
}

actual fun saveBackupFile(fileName: String, content: String, context: Any?): Boolean {
    val ctx = (context as? Context) ?: AndroidContextHolder.appContext ?: return false
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = ctx.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false
            resolver.openOutputStream(uri)?.use { os ->
                os.write(content.toByteArray())
            }
            true
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { os ->
                os.write(content.toByteArray())
            }
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

actual fun readBackupFile(context: Any?, onResult: (String?) -> Unit) {
    val ctx = (context as? Context) ?: AndroidContextHolder.appContext
    try {
        var content: String? = null

        // 1. Try MediaStore on Android 10+
        if (ctx != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = ctx.contentResolver
            val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME)
            val selection = "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%richfarmaci%.json")
            val sortOrder = "${MediaStore.Downloads.DATE_ADDED} DESC"

            resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                    content = resolver.openInputStream(contentUri)?.bufferedReader()?.use { it.readText() }
                }
            }
        }

        // 2. Fallback to direct file in Downloads
        if (content == null) {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir.exists() && downloadsDir.isDirectory) {
                val file = downloadsDir.listFiles()
                    ?.filter { it.name.lowercase().contains("richfarmaci") && it.name.endsWith(".json") }
                    ?.maxByOrNull { it.lastModified() }
                    ?: File(downloadsDir, "RichFarmaciW_Backup.json")
                if (file.exists() && file.canRead()) {
                    content = file.readText()
                }
            }
        }

        onResult(content)
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(null)
    }
}
