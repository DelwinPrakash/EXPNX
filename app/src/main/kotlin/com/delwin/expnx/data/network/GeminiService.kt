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

data class MetricItem(val label: String, val value: String)

data class GeminiRecommendation(
    val id: String,
    val title: String,
    val subtitle: String,
    val message: String,
    val actionText: String,
    val iconName: String,
    val summary: String,
    val metrics: List<MetricItem>,
    val tips: List<String>
)

data class SpendingInsightResponse(
    val general_insight: String,
    val category_insights: Map<String, String>,
    val budget_recommendation: String? = null,
    val goal_recommendation: String? = null,
    val expected_end_of_month_balance: String? = null,
    val forecasted_spending: String? = null,
    val upcoming_expense_prediction: String? = null,
    val cash_flow_risk_alert: String? = null,
    val recommendations: List<GeminiRecommendation>? = null
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
            You will receive the user's monthly budget, total spending, a breakdown of their expenses per category (with limit and actual spend), active financial goals, and upcoming/active bills.
            Analyze their spending behavior, budgets, goals, and bills to return a JSON object with:
            1. "general_insight": A short, motivating, and punchy overall spending insight (maximum 15 words) that highlights a significant pattern or positive trend.
            2. "category_insights": A JSON object mapping Category names (FOOD, TRANSPORT, SHOPPING, HEALTH, ENTERTAINMENT, BILLS, OTHER) to specific, actionable, and personalized category-level recommendations or comments (maximum 25 words per category).
            3. "budget_recommendation": A specific, personalized suggestion (maximum 25 words) for setting or optimizing category budgets based on recent spending patterns.
            4. "goal_recommendation": A specific, motivating suggestion (maximum 25 words) to help achieve active goals faster, referencing one of their active goal names and suggesting dining/shopping cuts. If no active goals are set, return a generic goal saving recommendation.
            5. "expected_end_of_month_balance": A predicted end of month balance based on current spending rate and active budgets (e.g. "₹42,300").
            6. "forecasted_spending": A predicted total spending for this month (e.g. "₹12,500").
            7. "upcoming_expense_prediction": A predicted upcoming expenses amount from active bills (e.g. "₹4,000").
            8. "cash_flow_risk_alert": A cash-flow risk assessment (either "Low Risk", "Medium Risk", or "High Risk").
            9. "recommendations": A JSON array of exactly 3 detailed recommendations. Each recommendation MUST contain:
               - "id": A unique string id (e.g. "food", "subscriptions", "spikes").
               - "title": A descriptive title.
               - "subtitle": A category/alert description.
               - "message": A short 1-sentence card description.
               - "actionText": Action button label (e.g. "Review", "View Subs", "Analyze").
               - "iconName": One of "Warning", "Star", "Info".
               - "summary": A detailed summary explaining the analysis.
               - "metrics": A list of exactly 3 metric items, each with "label" and "value" strings.
               - "tips": A list of 2-3 specific financial action tips.
            
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
              },
              "budget_recommendation": "Specific budget suggestion.",
              "goal_recommendation": "Specific goal achievement tip.",
              "expected_end_of_month_balance": "₹45,200",
              "forecasted_spending": "₹12,500",
              "upcoming_expense_prediction": "₹4,000",
              "cash_flow_risk_alert": "Low Risk",
              "recommendations": [
                {
                  "id": "food",
                  "title": "Food Expense Analysis",
                  "subtitle": "Budget Alert & Recommendation",
                  "message": "You spent 22% more on food this month",
                  "actionText": "Review",
                  "iconName": "Warning",
                  "summary": "...",
                  "metrics": [
                    { "label": "Current Spend", "value": "₹9,780" },
                    { "label": "Budget Limit", "value": "₹8,000" },
                    { "label": "Deviation", "value": "+₹1,780 (+22%)" }
                  ],
                  "tips": [
                    "Dining out spikes on Friday and Saturday nights contribute to 45% of the excess spend.",
                    "💡 Tip: Try meal prepping for weekends."
                  ]
                },
                ...
              ]
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
