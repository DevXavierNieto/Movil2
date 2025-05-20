package org.example.microproyecto

import org.jetbrains.compose.web.css.*

object PokemonStyles : StyleSheet() {

    val container by style {
        property("max-width", "600px")
        property("margin", "0 auto")
        padding(16.px)

        height(100.vh)
        property("overflow-y", "auto")
    }


    val card by style {
        backgroundColor(Color("#f4f0fa"))
        borderRadius(12.px)
        padding(16.px)
        marginBottom(16.px)
        textAlign("center")
        property("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
    }

    val image by style {
        width(200.px)
        height(200.px)
        property("object-fit", "contain")
        marginBottom(12.px)
    }


}
