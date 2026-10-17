package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Conversation",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val selectedModel: String = "gemini-3.8-flash"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: String, // "user", "model", or "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaType: String? = null, // "image", "audio", "video", or null
    val mediaData: String? = null, // Base64 or URL or file path
    val isThinking: Boolean = false,
    val isGrounded: Boolean = false,
    val groundingSource: String? = null, // "googleSearch", "googleMaps"
    val isError: Boolean = false
)
