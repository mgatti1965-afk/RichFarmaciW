package com.aistudio.richfarmaciw.util

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.stringWithContentsOfFile

actual fun saveBackupFile(fileName: String, content: String, context: Any?): Boolean {
    return try {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentDir = urls.firstOrNull() as? platform.Foundation.NSURL ?: return false
        val fileUrl = documentDir.URLByAppendingPathComponent(fileName) ?: return false
        val nsData = (content as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return false
        fileManager.createFileAtPath(fileUrl.path!!, nsData, null)
        true
    } catch (_: Exception) {
        false
    }
}

actual fun readBackupFile(context: Any?, onResult: (String?) -> Unit) {
    try {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentDir = urls.firstOrNull() as? platform.Foundation.NSURL ?: run {
            onResult(null)
            return
        }
        val fileUrl = documentDir.URLByAppendingPathComponent("backup_richfarmaci.json") ?: run {
            onResult(null)
            return
        }
        val path = fileUrl.path
        if (path != null && fileManager.fileExistsAtPath(path)) {
            val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
            onResult(content as? String)
        } else {
            onResult(null)
        }
    } catch (_: Exception) {
        onResult(null)
    }
}
