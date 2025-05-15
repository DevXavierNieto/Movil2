package org.example.microproyecto

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MicroProyecto",
    ) {
        App()
    }
}