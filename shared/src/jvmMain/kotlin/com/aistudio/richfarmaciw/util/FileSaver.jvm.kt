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

actual fun readBackupFile(context: Any?, onResult: (String?) -> Unit) {
    try {
        val downloadsDir = File(System.getProperty("user.home"), "Downloads")
        val file = downloadsDir.listFiles()
            ?.filter { it.name.contains("backup_richfarmaci") && it.name.endsWith(".json") }
            ?.maxByOrNull { it.lastModified() }
            ?: File(downloadsDir, "backup_richfarmaci.json")
        if (file.exists() && file.canRead()) {
            onResult(file.readText())
        } else {
            onResult(null)
        }
    } catch (_: Exception) {
        onResult(null)
    }
}
