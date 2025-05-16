package org.example.microproyecto

import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        PokemonListScreenWeb()
    }
}
