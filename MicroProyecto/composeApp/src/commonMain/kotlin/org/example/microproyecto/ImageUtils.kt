// commonMain/kotlin/org/example/microproyecto/ImageUtils.kt
package org.example.microproyecto

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun loadImageBitmapFromUrl(url: String): ImageBitmap?
