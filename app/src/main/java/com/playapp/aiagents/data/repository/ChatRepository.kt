package com.playapp.aiagents.data.repository

import android.content.Context
import androidx.room.Room
import com.playapp.aiagents.data.database.ChatDatabase
import com.playapp.aiagents.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ChatRepository(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        ChatDatabase::class.java,
        ChatDatabase.DATABASE_NAME
    ).build()

    private val sessionDao = database.chatSessionDao()
    private val messageDao = database.chatMessageDao()

    // Session operations
    fun getAllSessions(): Flow<List<ChatSession>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toChatSession() }
        }
    }

    fun getSessionsForAgent(agentId: Int): Flow<List<ChatSession>> {
        return sessionDao.getSessionsForAgent(agentId).map { entities ->
            entities.map { it.toChatSession() }
        }
    }

    suspend fun getSessionById(sessionId: String): ChatSession? {
        return sessionDao.getSessionById(sessionId)?.toChatSession()
    }

    suspend fun createSession(agentId: Int, title: String): String {
        val sessionId = UUID.randomUUID().toString()
        val session = ChatSessionEntity(
            id = sessionId,
            agentId = agentId,
            title = title,
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        sessionDao.insertSession(session)
        return sessionId
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            val updatedSession = it.copy(
                title = title,
                lastModified = System.currentTimeMillis()
            )
            sessionDao.updateSession(updatedSession)
        }
    }

    suspend fun deleteSession(sessionId: String) {
        messageDao.deleteMessagesForSession(sessionId)
        sessionDao.deleteSessionById(sessionId)
    }

    suspend fun clearAllData() {
        messageDao.deleteAllMessages()
        sessionDao.deleteAllSessions()
    }

    // Message operations
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForSession(sessionId).map { entities ->
            entities.map { it.toChatMessage() }
        }
    }

    suspend fun addMessage(sessionId: String, message: ChatMessage) {
        val entity = ChatMessageEntity(
            id = message.id,
            sessionId = sessionId,
            content = message.content,
            isUser = message.isUser,
            timestamp = message.timestamp,
            model = message.model,
            performanceMetrics = null // Will be updated when response is received
        )
        messageDao.insertMessage(entity)

        // Update session last modified time
        updateSessionLastModified(sessionId)
    }

    suspend fun addMessageWithMetrics(
        sessionId: String,
        message: ChatMessage,
        metrics: com.playapp.aiagents.data.service.OllamaApiService.PerformanceMetrics?
    ) {
        val entity = ChatMessageEntity(
            id = message.id,
            sessionId = sessionId,
            content = message.content,
            isUser = message.isUser,
            timestamp = message.timestamp,
            model = message.model,
            performanceMetrics = metrics?.let {
                PerformanceMetrics(
                    totalDuration = it.totalDuration,
                    loadDuration = it.loadDuration,
                    promptEvalCount = it.promptEvalCount,
                    promptEvalDuration = it.promptEvalDuration,
                    evalCount = it.evalCount,
                    evalDuration = it.evalDuration,
                    tokensPerSecond = it.tokensPerSecond
                )
            }
        )
        messageDao.insertMessage(entity)

        // Update session last modified time
        updateSessionLastModified(sessionId)
    }

    suspend fun updateMessageMetrics(
        messageId: String,
        metrics: com.playapp.aiagents.data.service.OllamaApiService.PerformanceMetrics
    ) {
        val message = messageDao.getMessagesForSessionSync("").find { it.id == messageId }
        message?.let {
            val updatedMessage = it.copy(
                performanceMetrics = PerformanceMetrics(
                    totalDuration = metrics.totalDuration,
                    loadDuration = metrics.loadDuration,
                    promptEvalCount = metrics.promptEvalCount,
                    promptEvalDuration = metrics.promptEvalDuration,
                    evalCount = metrics.evalCount,
                    evalDuration = metrics.evalDuration,
                    tokensPerSecond = metrics.tokensPerSecond
                )
            )
            messageDao.updateMessage(updatedMessage)
        }
    }

    private suspend fun updateSessionLastModified(sessionId: String) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            val updatedSession = it.copy(lastModified = System.currentTimeMillis())
            sessionDao.updateSession(updatedSession)
        }
    }

    // Helper functions to convert between entities and domain models
    private fun ChatSessionEntity.toChatSession(): ChatSession {
        return ChatSession(
            id = id,
            agentId = agentId,
            title = title,
            messages = emptyList(), // Messages loaded separately
            createdAt = createdAt,
            lastModified = lastModified
        )
    }

    private fun ChatMessageEntity.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            isUser = isUser,
            timestamp = timestamp,
            model = model
        )
    }
}