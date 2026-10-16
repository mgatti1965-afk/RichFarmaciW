package com.aistudio.richfarmaciw.util

import kotlinx.browser.localStorage

actual fun webGetItem(key: String): String? = localStorage.getItem(key)
actual fun webSetItem(key: String, value: String) {
    localStorage.setItem(key, value)
}
actual fun webRemoveItem(key: String) {
    localStorage.removeItem(key)
}
