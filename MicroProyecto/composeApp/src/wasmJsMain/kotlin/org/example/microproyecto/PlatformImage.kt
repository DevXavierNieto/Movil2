package org.example.microproyecto

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.attributes.src
import org.jetbrains.compose.web.attributes.alt

@Composable
actual fun PlatformImage(url: String, altText: String) {
    Img(src = url, alt = altText)
}
