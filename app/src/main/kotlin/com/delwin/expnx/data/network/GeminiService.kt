package com.delwin.expnx.data.network

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: SystemInstruction? = null,
    val generationConfig: GenerationConfig? = null
) {
    data class Content(val parts: List<Part>)
    data class Part(val text: String)
    data class SystemInstruction(val parts: List<Part>)
    data class GenerationConfig(val responseMimeType: String)
}

data class GeminiResponse(
    val candidates: List<Candidate>
) {
    data class Candidate(val content: Content)
    data class Content(val parts: List<Part>)
    data class Part(val text: String)
}

data class SpendingInsightResponse(
    val general_insight: String,
    val category_insights: Map<String, String>
)

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun getSpendingInsights(promptContent: String): SpendingInsightResponse {
        val apiKey = GeminiConfig.API_KEY
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            throw IllegalStateException("Gemini API key is not configured. Please add 'GEMINI_API_KEY=your_key' to your key.properties file.")
        }

        val systemPrompt = """
            You are an AI financial advisor for a personal finance tracker app named EXPNX. 
            You will receive the user's monthly budget, total spending, and a breakdown of their expenses per category (with limit and actual spend).
            Analyze their spending behavior and return a JSON object with:
            1. "general_insight": A short, motivating, and punchy overall spending insight (maximum 15 words) that highlights a significant pattern or positive trend.
            2. "category_insights": A JSON object mapping Category names (FOOD, TRANSPORT, SHOPPING, HEALTH, ENTERTAINMENT, BILLS, OTHER) to specific, actionable, and personalized category-level recommendations or comments (maximum 25 words per category).
            
            Format of the output JSON response MUST be exactly:
            {
              "general_insight": "Your overall trend description.",
              "category_insights": {
                "FOOD": "Specific recommendation for food.",
                "TRANSPORT": "Specific recommendation for transport.",
                "SHOPPING": "...",
                "HEALTH": "...",
                "ENTERTAINMENT": "...",
                "BILLS": "...",
                "OTHER": "..."
              }
            }
        """.trimIndent()

        val requestBodyObject = GeminiRequest(
            contents = listOf(
                GeminiRequest.Content(
                    parts = listOf(GeminiRequest.Part(text = promptContent))
                )
            ),
            systemInstruction = GeminiRequest.SystemInstruction(
                parts = listOf(GeminiRequest.Part(text = systemPrompt))
            ),
            generationConfig = GeminiRequest.GenerationConfig(responseMimeType = "application/json")
        )

        val requestBodyString = gson.toJson(requestBodyObject)
        val body = requestBodyString.toRequestBody(mediaType)

        val url = "${GeminiConfig.BASE_URL}?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IOException("Gemini API call failed with code ${response.code}: $errorBody")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response from Gemini")
            val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
            
            val content = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IOException("No generated content found in Gemini response")

            return gson.fromJson(content, SpendingInsightResponse::class.java)
        }
    }
}
