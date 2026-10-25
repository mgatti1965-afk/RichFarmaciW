package com.aistudio.richfarmaciw

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        val target = document.getElementById("ComposeTarget") ?: document.body!!
        ComposeViewport(target) {
            App()
        }
    }
}
