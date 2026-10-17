package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class GeminiResult<out T> {
    data class Success<T>(val data: T) : GeminiResult<T>()
    data class Error(val message: String, val isApiKeyMissing: Boolean = false) : GeminiResult<Nothing>()
}

data class GeneratedImageResult(
    val base64Data: String,
    val mimeType: String,
    val description: String? = null
)

data class GeneratedMediaResult(
    val type: String, // "image", "audio", "video"
    val base64Data: String? = null,
    val textExplanation: String
)

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
    }

    private fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Nana AI System Instruction establishing her friendly, clever, curious persona.
     */
    private val nanaSystemInstruction = """
        You are Nana AI, a friendly, clever, and delightfully curious AI assistant.
        Your personality traits:
        - Warm, encouraging, and witty: You bring a spark of warmth and thoughtful cleverness to every reply.
        - Naturally curious: You often point out fascinating angles, ask thoughtful follow-ups, and enjoy diving into details.
        - Clear & Structured: Use formatting, bullet points, numbered lists, and bold text to make your points easy to read.
        - Concise yet complete: Answer the user's questions directly without unnecessary filler, but offer deeper insight if helpful.
        - Highly knowledgeable: You excel in brainstorming, coding, explaining complex concepts, writing, science, everyday planning, and creative thinking.
    """.trimIndent()

    /**
     * Generate content with multi-turn conversation context.
     * Supports:
     * - model: gemini-3.8-flash (default), gemini-4-argon (with thinking), gemini-3.1-flash-lite (fast)
     * - tools: googleSearch, googleMaps
     * - thinkingLevel: HIGH for gemini-4-argon
     */
    suspend fun generateChatResponse(
        history: List<Pair<String, String>>, // role to content
        newMessage: String,
        modelName: String = "gemini-3.8-flash",
        enableThinking: Boolean = false,
        enableSearchGrounding: Boolean = false,
        enableMapsGrounding: Boolean = false
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext GeminiResult.Error(
                "Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio.",
                isApiKeyMissing = true
            )
        }

        val effectiveModel = when {
            enableThinking -> "gemini-4-argon"
            modelName.isNotBlank() -> modelName
            else -> "gemini-3.8-flash"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$effectiveModel:generateContent?key=${getApiKey()}"

        try {
            val root = JSONObject()

            // System instruction
            val systemObj = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().put("text", nanaSystemInstruction))
                }
                put("parts", partsArray)
            }
            root.put("systemInstruction", systemObj)

            // Contents array with conversation context
            val contentsArray = JSONArray()

            // Prior history turns (capped at last 16 turns for optimal token balance)
            val trimmedHistory = history.takeLast(16)
            for ((role, text) in trimmedHistory) {
                val turnRole = if (role.lowercase() == "user") "user" else "model"
                val turnObj = JSONObject().apply {
                    put("role", turnRole)
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", text))
                    })
                }
                contentsArray.put(turnObj)
            }

            // Current user message
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", newMessage))
                })
            }
            contentsArray.put(currentTurn)
            root.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            if (enableThinking || effectiveModel == "gemini-4-argon") {
                val thinkingConfig = JSONObject().apply {
                    put("thinkingLevel", "HIGH")
                }
                genConfig.put("thinkingConfig", thinkingConfig)
            }
            root.put("generationConfig", genConfig)

            // Tools (Search / Maps Grounding)
            val toolsArray = JSONArray()
            if (enableSearchGrounding && !enableThinking) {
                toolsArray.put(JSONObject().put("googleSearch", JSONObject()))
            }
            if (enableMapsGrounding && !enableThinking) {
                toolsArray.put(JSONObject().put("googleMaps", JSONObject()))
            }
            if (toolsArray.length() > 0) {
                root.put("tools", toolsArray)
            }

            val requestBody = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errMsg = parseErrorMessage(responseString, response.code)
                return@withContext GeminiResult.Error(errMsg)
            }

            val parsedText = parseCandidateText(responseString)
            if (parsedText.isBlank()) {
                return@withContext GeminiResult.Error("Nana received an empty response. Please try again.")
            }

            GeminiResult.Success(parsedText)
        } catch (e: IOException) {
            Log.e("GeminiService", "Network error", e)
            GeminiResult.Error("Network error: Please check your internet connection. (${e.localizedMessage})")
        } catch (e: Exception) {
            Log.e("GeminiService", "Unexpected error", e)
            GeminiResult.Error("Error: ${e.localizedMessage ?: "Unknown error occurred"}")
        }
    }

    /**
     * Generate high-quality images using gemini-3-pro-image-preview or gemini-nano-banana-2.1
     * Allows user to select imageSize (1K, 2K, 4K) and aspectRatio (1:1, 16:9, 9:16).
     */
    suspend fun generateImage(
        prompt: String,
        modelName: String = "gemini-3-pro-image-preview",
        imageSize: String = "1K", // "1K", "2K", "4K"
        aspectRatio: String = "1:1" // "1:1", "16:9", "9:16", "4:3", "3:4"
    ): GeminiResult<GeneratedImageResult> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext GeminiResult.Error(
                "Gemini API key is not configured. Add GEMINI_API_KEY in the Secrets panel in AI Studio.",
                isApiKeyMissing = true
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=${getApiKey()}"

        try {
            val root = JSONObject()
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            }
            root.put("contents", contents)

            val genConfig = JSONObject().apply {
                put("responseModalities", JSONArray().apply {
                    put("TEXT")
                    put("IMAGE")
                })
                val imageConfig = JSONObject().apply {
                    put("aspectRatio", aspectRatio)
                    put("imageSize", imageSize)
                }
                put("imageConfig", imageConfig)
            }
            root.put("generationConfig", genConfig)

            val requestBody = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(requestBody).build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errMsg = parseErrorMessage(responseString, response.code)
                return@withContext GeminiResult.Error(errMsg)
            }

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null) {
                    var imageBase64: String? = null
                    var mimeType = "image/png"
                    var textDesc: String? = null

                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("inlineData")) {
                            val inline = part.getJSONObject("inlineData")
                            imageBase64 = inline.optString("data")
                            mimeType = inline.optString("mimeType", "image/png")
                        } else if (part.has("text")) {
                            textDesc = part.optString("text")
                        }
                    }

                    if (!imageBase64.isNullOrBlank()) {
                        return@withContext GeminiResult.Success(
                            GeneratedImageResult(
                                base64Data = imageBase64,
                                mimeType = mimeType,
                                description = textDesc
                            )
                        )
                    }
                }
            }

            GeminiResult.Error("No image was returned. You can try refining the prompt.")
        } catch (e: Exception) {
            Log.e("GeminiService", "Image generation failed", e)
            GeminiResult.Error("Image generation failed: ${e.localizedMessage}")
        }
    }

    /**
     * Generate Music track or clip using Lyria models:
     * lyria-3-clip-preview (up to 30s) or lyria-3-pro-preview (full track)
     */
    suspend fun generateMusic(
        prompt: String,
        modelName: String = "lyria-3-clip-preview"
    ): GeminiResult<GeneratedMediaResult> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext GeminiResult.Error(
                "Gemini API key is not configured.",
                isApiKeyMissing = true
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=${getApiKey()}"

        try {
            val root = JSONObject()
            root.put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            val genConfig = JSONObject().apply {
                put("responseModalities", JSONArray().apply {
                    put("AUDIO")
                })
            }
            root.put("generationConfig", genConfig)

            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext GeminiResult.Error(parseErrorMessage(responseString, response.code))
            }

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            var audioBase64: String? = null
            if (candidates != null && candidates.length() > 0) {
                val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("inlineData")) {
                            audioBase64 = part.getJSONObject("inlineData").optString("data")
                        }
                    }
                }
            }

            GeminiResult.Success(
                GeneratedMediaResult(
                    type = "audio",
                    base64Data = audioBase64,
                    textExplanation = "Music generated with $modelName based on: \"$prompt\""
                )
            )
        } catch (e: Exception) {
            GeminiResult.Error("Music generation failed: ${e.localizedMessage}")
        }
    }

    /**
     * Animate image into video using Veo:
     * model: veo-3.1-fast-generate-preview (16:9 or 9:16)
     */
    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9", // "16:9" or "9:16"
        modelName: String = "veo-3.1-fast-generate-preview"
    ): GeminiResult<GeneratedMediaResult> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext GeminiResult.Error("Gemini API key is not configured.", isApiKeyMissing = true)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateVideos?key=${getApiKey()}"

        try {
            val root = JSONObject().apply {
                put("prompt", prompt)
                put("config", JSONObject().apply {
                    put("numberOfVideos", 1)
                    put("resolution", "720p")
                    put("aspectRatio", aspectRatio)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext GeminiResult.Error(parseErrorMessage(responseString, response.code))
            }

            val json = JSONObject(responseString)
            val opName = json.optString("name", "Veo Video Generation Job Started")

            GeminiResult.Success(
                GeneratedMediaResult(
                    type = "video",
                    textExplanation = "Veo Video animation initiated ($aspectRatio)! Operation: $opName"
                )
            )
        } catch (e: Exception) {
            GeminiResult.Error("Video generation failed: ${e.localizedMessage}")
        }
    }

    private fun parseCandidateText(responseString: String): String {
        return try {
            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""

            val builder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    builder.append(part.getString("text"))
                }
            }
            builder.toString().trim()
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseErrorMessage(errorBody: String, statusCode: Int): String {
        return try {
            val json = JSONObject(errorBody)
            val errorObj = json.optJSONObject("error")
            val message = errorObj?.optString("message") ?: ""
            when {
                statusCode == 400 && message.contains("API key", ignoreCase = true) ->
                    "Invalid API Key. Please verify your GEMINI_API_KEY in the Secrets panel."
                statusCode == 403 ->
                    "Access Forbidden. Please verify that your Gemini API key has access to the requested model."
                statusCode == 429 ->
                    "Rate limit reached. Please wait a moment before sending another request."
                statusCode >= 500 ->
                    "Gemini service is temporarily unavailable. Please try again shortly."
                message.isNotBlank() ->
                    message
                else ->
                    "Request failed with HTTP status $statusCode"
            }
        } catch (e: Exception) {
            "Request failed (HTTP $statusCode): $errorBody"
        }
    }
}
