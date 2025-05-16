// desktopMain/kotlin/org/example/microproyecto/ImageUtils.kt
package org.example.microproyecto

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import org.jetbrains.skia.Image

actual suspend fun loadImageBitmapFromUrl(url: String): ImageBitmap? {
    return try {
        val client = HttpClient(CIO)
        val bytes: ByteArray = client.get(url).body()
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
