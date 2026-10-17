package com.aistudio.richfarmaciw.util

import java.io.File

actual fun saveBackupFile(fileName: String, content: String, context: Any?): Boolean {
    return try {
        val downloadsDir = File(System.getProperty("user.home"), "Downloads")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        file.writeText(content)
        true
    } catch (_: Exception) {
        false
    }
}
