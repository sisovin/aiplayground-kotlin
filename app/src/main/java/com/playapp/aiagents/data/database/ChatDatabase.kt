package com.playapp.aiagents.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.playapp.aiagents.data.model.ChatMessageEntity
import com.playapp.aiagents.data.model.ChatSessionEntity

@Database(
    entities = [ChatSessionEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        const val DATABASE_NAME = "chat_database"
    }
}