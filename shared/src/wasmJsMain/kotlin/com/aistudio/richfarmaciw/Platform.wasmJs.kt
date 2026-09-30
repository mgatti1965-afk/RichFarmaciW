package com.aistudio.richfarmaciw

class WasmPlatform : Platform {
    override val name: String = "Web (Wasm)"
    override val isMobile: Boolean = false // Web is treated as non-mobile for intent logic
}

actual fun getPlatform(): Platform = WasmPlatform()
