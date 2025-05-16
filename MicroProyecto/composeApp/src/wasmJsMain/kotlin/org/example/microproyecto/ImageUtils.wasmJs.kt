package org.example.microproyecto

import androidx.compose.ui.graphics.ImageBitmap

actual suspend fun loadImageBitmapFromUrl(url: String): ImageBitmap? {
    // Imagen no soportada directamente en Compose Web/WASM
    return null
}
