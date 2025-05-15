package org.example.microproyecto

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformImage(url: String, altText: String = "")
