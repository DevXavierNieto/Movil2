package org.example.microproyecto

class WebPlatform : Platform {
    override val name: String = "Web (WASM)"
}

actual fun getPlatform(): Platform = WebPlatform()
