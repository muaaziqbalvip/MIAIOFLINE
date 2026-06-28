package com.miai.offline.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val role: String,              // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,  // which model id generated this response
    val isDeepThinking: Boolean = false
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val modelId: String
)
