package com.aistudio.richfarmaciw

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import richfarmaciw.shared.generated.resources.Res
import richfarmaciw.shared.generated.resources.app_icon

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RichFarmaciW",
        icon = painterResource(Res.drawable.app_icon),
    ) {
        App()
    }
}