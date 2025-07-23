package com.paloma.paloma

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform