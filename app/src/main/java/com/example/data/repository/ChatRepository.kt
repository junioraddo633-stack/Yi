package com.example.data.repository

import com.example.data.api.GeminiResult
import com.example.data.api.GeminiService
import com.example.data.local.ChatDao
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val geminiService: GeminiService
) {
    fun getAllSessions(): Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(
        title: String = "New Conversation",
        model: String = "gemini-3.8-flash"
    ): String {
        val newSession = ChatSession(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            selectedModel = model
        )
        chatDao.insertSession(newSession)
        return newSession.id
    }

    suspend fun ensureSessionExists(sessionId: String?): String {
        if (sessionId != null) {
            val existing = chatDao.getSessionById(sessionId)
            if (existing != null) return existing.id
        }
        return createNewSession()
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) {
        val session = chatDao.getSessionById(sessionId) ?: return
        chatDao.updateSession(session.copy(title = title, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearMessages(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }

    /**
     * Send user message and fetch multi-turn conversation response from Nana AI.
     */
    suspend fun sendMessage(
        sessionId: String,
        userText: String,
        modelName: String = "gemini-3.8-flash",
        enableThinking: Boolean = false,
        enableSearch: Boolean = false,
        enableMaps: Boolean = false
    ): GeminiResult<String> {
        // 1. Insert user message into local database
        val userMessage = ChatMessage(
            sessionId = sessionId,
            role = "user",
            content = userText,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userMessage)

        // Auto-update session title if it's the first message
        val currentHistory = chatDao.getMessagesSnapshot(sessionId)
        if (currentHistory.size <= 2) {
            val summaryTitle = if (userText.length > 28) userText.take(28) + "…" else userText
            updateSessionTitle(sessionId, summaryTitle)
        }

        // 2. Build history pairs for Gemini
        val historyPairs = currentHistory.dropLast(1).map { it.role to it.content }

        // 3. Call Gemini
        val result = geminiService.generateChatResponse(
            history = historyPairs,
            newMessage = userText,
            modelName = modelName,
            enableThinking = enableThinking,
            enableSearchGrounding = enableSearch,
            enableMapsGrounding = enableMaps
        )

        when (result) {
            is GeminiResult.Success -> {
                val aiMessage = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = result.data,
                    timestamp = System.currentTimeMillis(),
                    isThinking = enableThinking,
                    isGrounded = enableSearch || enableMaps,
                    groundingSource = when {
                        enableSearch -> "googleSearch"
                        enableMaps -> "googleMaps"
                        else -> null
                    }
                )
                chatDao.insertMessage(aiMessage)
                // Touch session updated time
                val session = chatDao.getSessionById(sessionId)
                if (session != null) {
                    chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
                }
            }
            is GeminiResult.Error -> {
                val errorMessage = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = result.message,
                    timestamp = System.currentTimeMillis(),
                    isError = true
                )
                chatDao.insertMessage(errorMessage)
            }
        }

        return result
    }

    /**
     * Generate an image via Gemini and insert into conversation thread.
     */
    suspend fun generateImage(
        sessionId: String,
        prompt: String,
        modelName: String = "gemini-3-pro-image-preview",
        imageSize: String = "1K",
        aspectRatio: String = "1:1"
    ): GeminiResult<String> {
        // Record user intent
        val userPromptMessage = ChatMessage(
            sessionId = sessionId,
            role = "user",
            content = "🎨 Generate Image ($imageSize, $aspectRatio): $prompt",
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userPromptMessage)

        val result = geminiService.generateImage(
            prompt = prompt,
            modelName = modelName,
            imageSize = imageSize,
            aspectRatio = aspectRatio
        )

        return when (result) {
            is GeminiResult.Success -> {
                val desc = result.data.description ?: "Here is the image I generated for you: \"$prompt\""
                val aiMessage = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = desc,
                    mediaType = "image",
                    mediaData = result.data.base64Data,
                    timestamp = System.currentTimeMillis()
                )
                chatDao.insertMessage(aiMessage)
                GeminiResult.Success(desc)
            }
            is GeminiResult.Error -> {
                val errorMsg = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = result.message,
                    timestamp = System.currentTimeMillis(),
                    isError = true
                )
                chatDao.insertMessage(errorMsg)
                result
            }
        }
    }

    /**
     * Generate music via Lyria and insert into chat.
     */
    suspend fun generateMusic(
        sessionId: String,
        prompt: String,
        modelName: String = "lyria-3-clip-preview"
    ): GeminiResult<String> {
        val userMsg = ChatMessage(
            sessionId = sessionId,
            role = "user",
            content = "🎵 Generate Music: $prompt",
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userMsg)

        val result = geminiService.generateMusic(prompt, modelName)
        return when (result) {
            is GeminiResult.Success -> {
                val aiMessage = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = "✨ Nana AI crafted this music clip for you!\n\n${result.data.textExplanation}",
                    mediaType = "audio",
                    mediaData = result.data.base64Data,
                    timestamp = System.currentTimeMillis()
                )
                chatDao.insertMessage(aiMessage)
                GeminiResult.Success(result.data.textExplanation)
            }
            is GeminiResult.Error -> {
                val err = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = result.message,
                    timestamp = System.currentTimeMillis(),
                    isError = true
                )
                chatDao.insertMessage(err)
                result
            }
        }
    }

    /**
     * Generate / Animate Video via Veo and insert into chat.
     */
    suspend fun generateVideo(
        sessionId: String,
        prompt: String,
        aspectRatio: String = "16:9"
    ): GeminiResult<String> {
        val userMsg = ChatMessage(
            sessionId = sessionId,
            role = "user",
            content = "🎬 Animate Video ($aspectRatio): $prompt",
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userMsg)

        val result = geminiService.generateVideo(prompt, aspectRatio)
        return when (result) {
            is GeminiResult.Success -> {
                val aiMessage = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = "🎬 ${result.data.textExplanation}\n\nNana AI has queued this video animation for prompt: \"$prompt\".",
                    mediaType = "video",
                    timestamp = System.currentTimeMillis()
                )
                chatDao.insertMessage(aiMessage)
                GeminiResult.Success(result.data.textExplanation)
            }
            is GeminiResult.Error -> {
                val err = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = result.message,
                    timestamp = System.currentTimeMillis(),
                    isError = true
                )
                chatDao.insertMessage(err)
                result
            }
        }
    }
}
