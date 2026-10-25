package com.aistudio.richfarmaciw

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        // Rimuove la schermata di caricamento una volta pronto WebAssembly Skia
        val loader = document.getElementById("loading-screen")
        loader?.remove()

        val target = document.getElementById("ComposeTarget") ?: document.body!!
        ComposeViewport(target) {
            App()
        }
    }
}
