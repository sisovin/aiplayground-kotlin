package com.playapp.aiagents.data.model

/*
 User settings model for dynamic settings management in SettingActivity.
 This model corresponds to the user_settings table in the database.
*/
data class UserSettings(
    val id: String = "",
    val userId: String = "",
    val theme: Theme = Theme.SYSTEM,
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val autoSaveConversations: Boolean = true,
    val defaultModel: String = "llama2",
    val streamingEnabled: Boolean = true,
    val maxTokens: Int = 2048,
    val geminiApiKey: String = "",
    val customSettings: Map<String, Any>? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

enum class Theme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}