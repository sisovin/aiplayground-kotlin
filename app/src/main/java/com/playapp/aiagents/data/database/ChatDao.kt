package com.playapp.aiagents.data.database

import androidx.room.*
import com.playapp.aiagents.data.model.ChatMessageEntity
import com.playapp.aiagents.data.model.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY lastModified DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE agentId = :agentId ORDER BY lastModified DESC")
    fun getSessionsForAgent(agentId: Int): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Delete
    suspend fun deleteSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSessionSync(sessionId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}