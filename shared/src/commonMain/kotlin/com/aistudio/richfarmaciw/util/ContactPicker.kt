package com.aistudio.richfarmaciw.util

import androidx.compose.runtime.Composable

data class ContactInfo(
    val name: String,
    val phoneOrEmail: String
)

interface ContactPickerLauncher {
    val isAvailable: Boolean
    fun launch()
}

@Composable
expect fun rememberContactPicker(
    onContactPicked: (ContactInfo) -> Unit
): ContactPickerLauncher
