package org.example.microproyecto

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.call.*

actual suspend fun loadImageBitmapFromUrl(url: String): ImageBitmap? {
    return try {
        val client = HttpClient(Android)
        val bytes: ByteArray = client.get(url).body()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
