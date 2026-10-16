package com.aistudio.richfarmaciw.util

expect fun webGetItem(key: String): String?
expect fun webSetItem(key: String, value: String)
expect fun webRemoveItem(key: String)
