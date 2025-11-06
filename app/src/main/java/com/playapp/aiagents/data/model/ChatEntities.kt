package com.playapp.aiagents.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.playapp.aiagents.data.database.Converters

@Entity(tableName = "chat_sessions")
@TypeConverters(Converters::class)
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val agentId: Int,
    val title: String,
    val createdAt: Long,
    val lastModified: Long
)

@Entity(tableName = "chat_messages")
@TypeConverters(Converters::class)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val model: String,
    val performanceMetrics: PerformanceMetrics? = null
)

data class PerformanceMetrics(
    val totalDuration: Long = 0,
    val loadDuration: Long = 0,
    val promptEvalCount: Int = 0,
    val promptEvalDuration: Long = 0,
    val evalCount: Int = 0,
    val evalDuration: Long = 0,
    val tokensPerSecond: Double = 0.0
)