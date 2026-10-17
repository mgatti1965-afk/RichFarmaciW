package com.aistudio.richfarmaciw.util

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
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
