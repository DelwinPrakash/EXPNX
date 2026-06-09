package com.delwin.expnx.data.network

import com.delwin.expnx.BuildConfig

object GeminiConfig {
    val API_KEY: String = BuildConfig.GEMINI_API_KEY
    const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent"
}
