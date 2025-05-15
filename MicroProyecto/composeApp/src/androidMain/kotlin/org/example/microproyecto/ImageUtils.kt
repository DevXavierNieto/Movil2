package org.example.microproyecto

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.BitmapPainter
import kotlinx.coroutines.*
import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.call.*

@Composable
actual fun PlatformImage(url: String, altText: String) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(url) {
        try {
            val client = HttpClient(Android)
            val bytes: ByteArray = client.get(url).body()
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            bitmap = null
        }
    }

    bitmap?.let {
        Image(
            painter = BitmapPainter(it.asImageBitmap()),
            contentDescription = altText,
            contentScale = ContentScale.Crop,
            modifier = Modifier
        )
    }
}
