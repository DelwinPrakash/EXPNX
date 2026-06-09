package com.delwin.expnx.data.network

import com.delwin.expnx.BuildConfig

object GeminiConfig {
    val API_KEY: String = BuildConfig.GEMINI_API_KEY
    private val availableModels = listOf(
        "gemini-3.5-flash",
        "gemini-3-flash-preview",
        "gemini-2.5-flash",
        "gemini-3.1-flash-lite",
        "gemini-2.5-flash-lite",
    )

    val BASE_URL: String
        get() = "https://generativelanguage.googleapis.com/v1beta/models/${availableModels.random()}:generateContent"
}
