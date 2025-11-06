package com.playapp.aiagents.data.service

import android.util.Log
import com.google.gson.Gson
import com.playapp.aiagents.data.model.ChatMessage
import com.playapp.aiagents.data.model.OllamaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class OllamaApiService(
    private val baseUrl: String = "http://localhost:11434",
    private val gson: Gson = Gson()
) {
    private val TAG = "OllamaApiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    data class ChatRequest(
        val model: String,
        val prompt: String,
        val stream: Boolean = true,
        val options: Map<String, Any> = emptyMap()
    )

    data class ChatResponse(
        val model: String,
        val created_at: String,
        val message: Message,
        val done: Boolean,
        val total_duration: Long? = null,
        val load_duration: Long? = null,
        val prompt_eval_count: Int? = null,
        val prompt_eval_duration: Long? = null,
        val eval_count: Int? = null,
        val eval_duration: Long? = null
    )

    data class Message(
        val role: String,
        val content: String
    )

    data class ModelInfo(
        val name: String,
        val size: Long,
        val digest: String,
        val details: ModelDetails
    )

    data class ModelDetails(
        val format: String,
        val family: String,
        val families: List<String>? = null,
        val parameter_size: String,
        val quantization_level: String
    )

    data class ListModelsResponse(
        val models: List<ModelInfo>
    )

    data class PerformanceMetrics(
        val totalDuration: Long,
        val loadDuration: Long,
        val promptEvalCount: Int,
        val promptEvalDuration: Long,
        val evalCount: Int,
        val evalDuration: Long,
        val tokensPerSecond: Double
    )

    /**
     * Get the correct Ollama model name from the enum
     */
    private fun getOllamaModelName(model: OllamaModel): String {
        return when (model) {
            OllamaModel.LLAMA2 -> "llama2"
            OllamaModel.MISTRAL -> "mistral"
            OllamaModel.CODELLAMA -> "codellama"
            OllamaModel.NEURAL_CHAT -> "neural-chat"
            OllamaModel.VICUNA -> "vicuna"
            OllamaModel.ORCA -> "orca-mini"
        }
    }

    /**
     * Send a chat message to Ollama and get streaming response
     */
    fun sendChatMessageStreaming(
        message: String,
        model: OllamaModel,
        systemPrompt: String = "",
        contextLength: Int = 4096
    ): Flow<Pair<String, PerformanceMetrics?>> = flow {
        try {
            val fullPrompt = if (systemPrompt.isNotEmpty()) {
                "$systemPrompt\n\nUser: $message\nAssistant:"
            } else {
                message
            }

            val requestBody = ChatRequest(
                model = getOllamaModelName(model),
                prompt = fullPrompt,
                stream = true,
                options = mapOf(
                    "num_ctx" to contextLength
                )
            )

            val request = Request.Builder()
                .url("$baseUrl/api/generate")
                .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            response.body?.let { body ->
                body.byteStream().bufferedReader().use { reader ->
                    var fullResponse = ""
                    var finalMetrics: PerformanceMetrics? = null
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        line?.let { currentLine ->
                            if (currentLine.isNotBlank()) {
                                try {
                                    val chatResponse = gson.fromJson(currentLine, ChatResponse::class.java)
                                    val chunk = chatResponse.message.content
                                    fullResponse += chunk
                                    emit(Pair(fullResponse, null))

                                    if (chatResponse.done) {
                                        finalMetrics = PerformanceMetrics(
                                            totalDuration = chatResponse.total_duration ?: 0,
                                            loadDuration = chatResponse.load_duration ?: 0,
                                            promptEvalCount = chatResponse.prompt_eval_count ?: 0,
                                            promptEvalDuration = chatResponse.prompt_eval_duration ?: 0,
                                            evalCount = chatResponse.eval_count ?: 0,
                                            evalDuration = chatResponse.eval_duration ?: 0,
                                            tokensPerSecond = calculateTokensPerSecond(
                                                chatResponse.eval_count ?: 0,
                                                chatResponse.eval_duration ?: 1
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing response line: $currentLine", e)
                                }
                            }
                        }
                    }

                    // Emit final result with metrics
                    emit(Pair(fullResponse, finalMetrics))
                }
            } ?: throw IOException("Empty response body")

        } catch (e: Exception) {
            Log.e(TAG, "Error in streaming chat", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Send a chat message to Ollama and get complete response (non-streaming)
     */
    suspend fun sendChatMessage(
        message: String,
        model: OllamaModel,
        systemPrompt: String = "",
        contextLength: Int = 4096
    ): Pair<String, PerformanceMetrics?> = withContext(Dispatchers.IO) {
        try {
            val fullPrompt = if (systemPrompt.isNotEmpty()) {
                "$systemPrompt\n\nUser: $message\nAssistant:"
            } else {
                message
            }

            val requestBody = ChatRequest(
                model = getOllamaModelName(model),
                prompt = fullPrompt,
                stream = false,
                options = mapOf(
                    "num_ctx" to contextLength
                )
            )

            val request = Request.Builder()
                .url("$baseUrl/api/generate")
                .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            response.body?.let { body ->
                val responseText = body.string()
                val chatResponse = gson.fromJson(responseText, ChatResponse::class.java)

                val metrics = PerformanceMetrics(
                    totalDuration = chatResponse.total_duration ?: 0,
                    loadDuration = chatResponse.load_duration ?: 0,
                    promptEvalCount = chatResponse.prompt_eval_count ?: 0,
                    promptEvalDuration = chatResponse.prompt_eval_duration ?: 0,
                    evalCount = chatResponse.eval_count ?: 0,
                    evalDuration = chatResponse.eval_duration ?: 0,
                    tokensPerSecond = calculateTokensPerSecond(
                        chatResponse.eval_count ?: 0,
                        chatResponse.eval_duration ?: 1
                    )
                )

                Pair(chatResponse.message.content, metrics)
            } ?: throw IOException("Empty response body")

        } catch (e: Exception) {
            Log.e(TAG, "Error in chat", e)
            throw e
        }
    }

    /**
     * Get list of available models from Ollama
     */
    suspend fun getAvailableModels(): List<ModelInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/tags")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            response.body?.let { body ->
                val responseText = body.string()
                val modelsResponse = gson.fromJson(responseText, ListModelsResponse::class.java)
                modelsResponse.models
            } ?: emptyList()

        } catch (e: Exception) {
            Log.e(TAG, "Error getting available models", e)
            emptyList()
        }
    }

    /**
     * Check if Ollama server is running
     */
    suspend fun isServerRunning(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/tags")
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error checking server status", e)
            false
        }
    }

    /**
     * Pull a model from Ollama registry
     */
    suspend fun pullModel(modelName: String): Flow<String> = flow {
        try {
            val requestBody = mapOf("name" to modelName)
            val request = Request.Builder()
                .url("$baseUrl/api/pull")
                .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            response.body?.let { body ->
                body.byteStream().bufferedReader().use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let { currentLine ->
                            if (currentLine.isNotBlank()) {
                                try {
                                    val progress = gson.fromJson(currentLine, Map::class.java)
                                    val status = progress["status"] as? String ?: ""
                                    emit(status)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing pull progress: $currentLine", e)
                                }
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error pulling model", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    private fun calculateTokensPerSecond(tokenCount: Int, durationNs: Long): Double {
        val durationSeconds = durationNs / 1_000_000_000.0
        return if (durationSeconds > 0) tokenCount / durationSeconds else 0.0
    }
}