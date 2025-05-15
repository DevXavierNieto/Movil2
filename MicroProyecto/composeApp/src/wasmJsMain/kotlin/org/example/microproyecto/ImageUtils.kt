package org.example.microproyecto

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformImage(url: String, altText: String) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(url) {
        bitmap = loadImageBitmapFromUrl(url)
    }

    bitmap?.let {
        Image(
            painter = BitmapPainter(it),
            contentDescription = altText,
            contentScale = ContentScale.Crop
        )
    }
}
