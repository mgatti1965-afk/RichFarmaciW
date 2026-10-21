package com.aistudio.richfarmaciw.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class WebContactPickerLauncher : ContactPickerLauncher {
    override val isAvailable: Boolean = false
    override fun launch() { }
}

@Composable
actual fun rememberContactPicker(
    onContactPicked: (ContactInfo) -> Unit
): ContactPickerLauncher {
    return remember { WebContactPickerLauncher() }
}
