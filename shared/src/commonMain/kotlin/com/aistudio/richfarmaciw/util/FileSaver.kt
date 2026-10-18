package com.aistudio.richfarmaciw.util

expect fun saveBackupFile(fileName: String, content: String, context: Any? = null): Boolean
expect fun readBackupFile(context: Any? = null, onResult: (String?) -> Unit)
