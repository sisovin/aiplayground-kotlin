package com.playapp.aiagents.data.model

/*
 Provide default values so Firebase's DataSnapshot -> object mapping can
 instantiate the class (Firebase requires a no-arg constructor / default values).
*/
data class Agent(
    val id: Int = 0,
    val title: String = "",
    val provider: String = "",
    val instructor: String = "",
    val duration: String = "",
    val description: String = "",
    val color: String = "#FFFFFF", // Color as hex string
    val topics: List<String> = emptyList(),
    val ollamaPrompt: String = "",
    val model: String = "",
    val modelType: String = "LLAMA2",
    val samplePrompts: List<String> = emptyList(),
    val setupInstructions: String = "",
    val supportsStreaming: Boolean = true,
    val videoTutorials: List<VideoTutorial> = emptyList(),
    val codeExamples: List<CodeExample> = emptyList()
)

enum class OllamaModel(val displayName: String, val description: String) {
    LLAMA2("Llama 2", "General purpose conversational AI"),
    MISTRAL("Mistral", "Fast and efficient language model"),
    CODELLAMA("Code Llama", "Specialized for code generation and understanding"),
    NEURAL_CHAT("Neural Chat", "Optimized for conversational AI"),
    VICUNA("Vicuna", "Fine-tuned conversational model"),
    ORCA("Orca", "Research-focused model")
}

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = ""
)

data class ChatSession(
    val id: String = "",
    val agentId: Int = 0,
    val title: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

data class UserProgress(
    val userId: String = "",
    val courses: Map<String, CourseProgress> = emptyMap(),
    val totalTimeSpent: Long = 0, // in milliseconds
    val lastActive: Long = System.currentTimeMillis(),
    val achievements: List<String> = emptyList()
)

data class CourseProgress(
    val courseId: Int = 0,
    val completionPercentage: Float = 0f, // 0.0 to 1.0
    val timeSpent: Long = 0, // in milliseconds
    val lastAccessed: Long = System.currentTimeMillis(),
    val completedTopics: List<String> = emptyList(),
    val agentProgress: Map<String, AgentProgress> = emptyMap(),
    val downloadedCode: Boolean = false,
    val watchedVideos: List<String> = emptyList()
)

data class AgentProgress(
    val agentId: Int = 0,
    val sessionsCount: Int = 0,
    val totalMessages: Int = 0,
    val timeSpent: Long = 0, // in milliseconds
    val lastUsed: Long = System.currentTimeMillis(),
    val favoritePrompts: List<String> = emptyList(),
    val skillLevel: Int = 1 // 1-5 skill level
)

data class VideoTutorial(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val duration: String = "", // e.g., "10:30"
    val thumbnailUrl: String = "",
    val order: Int = 0
)

data class CodeExample(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val language: String = "", // e.g., "kotlin", "python", "javascript"
    val code: String = "",
    val downloadUrl: String = "",
    val fileName: String = ""
)