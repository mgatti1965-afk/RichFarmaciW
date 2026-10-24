package com.aistudio.richfarmaciw

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val target = document.getElementById("ComposeTarget") ?: document.body!!
    ComposeViewport(target) {
        App()
    }
}
