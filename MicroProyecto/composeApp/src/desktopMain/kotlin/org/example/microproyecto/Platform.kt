package org.example.microproyecto

class JVMPlatform : Platform {
    override val name: String = "JVM ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()
