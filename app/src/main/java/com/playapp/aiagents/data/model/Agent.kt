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
    val modelType: OllamaModel = OllamaModel.LLAMA2,
    val samplePrompts: List<String> = emptyList(),
    val setupInstructions: String = "",
    val supportsStreaming: Boolean = true
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