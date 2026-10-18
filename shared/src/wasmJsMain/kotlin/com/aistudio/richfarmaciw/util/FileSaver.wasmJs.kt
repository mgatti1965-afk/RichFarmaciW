package com.aistudio.richfarmaciw.util

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import kotlin.js.JsArray
import kotlin.js.JsAny
import kotlin.js.toJsString

actual fun saveBackupFile(fileName: String, content: String, context: Any?): Boolean {
    return try {
        val parts = JsArray<JsAny?>()
        parts[0] = content.toJsString()
        val blob = Blob(parts, BlobPropertyBag(type = "application/json"))
        val url = URL.createObjectURL(blob)
        val a = document.createElement("a") as HTMLAnchorElement
        a.href = url
        a.download = fileName
        document.body?.appendChild(a)
        a.click()
        document.body?.removeChild(a)
        URL.revokeObjectURL(url)
        true
    } catch (_: Exception) {
        false
    }
}

actual fun readBackupFile(context: Any?, onResult: (String?) -> Unit) {
    try {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = ".json,application/json"
        input.onchange = {
            val file = input.files?.item(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    val text = reader.result?.toString()
                    onResult(text)
                }
                reader.onerror = {
                    onResult(null)
                }
                reader.readAsText(file)
            } else {
                onResult(null)
            }
        }
        input.click()
    } catch (_: Exception) {
        onResult(null)
    }
}
