package com.aistudio.richfarmaciw

interface Platform {
    val name: String
    val isMobile: Boolean
}

expect fun getPlatform(): Platform