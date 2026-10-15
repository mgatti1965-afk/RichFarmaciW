package com.aistudio.richfarmaciw

import kotlinx.browser.window

class JsPlatform : Platform {
    private val userAgent = try { window.navigator.userAgent } catch (_: Throwable) { "" }
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
        ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
        ?: "Web (JS)"

    override val isMobile: Boolean = false
}

actual fun getPlatform(): Platform = JsPlatform()
