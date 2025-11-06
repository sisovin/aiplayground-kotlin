package com.playapp.aiagents.data.service

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class FirebaseFunctionsService {
    private val TAG = "FirebaseFunctionsService"
    private val functions = FirebaseFunctions.getInstance()

    // Use emulator in debug mode (optional)
    init {
        // Uncomment for local development with Firebase Functions emulator
        // functions.useEmulator("10.0.2.2", 5001)
    }

    // Call a cloud function
    suspend fun callFunction(functionName: String, data: Map<String, Any>? = null): Result<Any?> {
        return try {
            val result = functions.getHttpsCallable(functionName).call(data).await()
            Result.success(result.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling function $functionName", e)
            Result.failure(e)
        }
    }

    // Process chat with AI using Firebase Functions
    suspend fun processChatWithAI(message: String, agentId: Int, context: List<Map<String, Any>>? = null): Result<Map<String, Any>> {
        return try {
            val data = mapOf(
                "message" to message,
                "agentId" to agentId,
                "context" to (context ?: emptyList()),
                "timestamp" to System.currentTimeMillis()
            )

            val result = callFunction("processChatWithAI", data)
            result.map { it as Map<String, Any> }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing chat with AI", e)
            Result.failure(e)
        }
    }

    // Generate code examples using Firebase Functions
    suspend fun generateCodeExample(agentId: Int, language: String, prompt: String): Result<Map<String, Any>> {
        return try {
            val data = mapOf(
                "agentId" to agentId,
                "language" to language,
                "prompt" to prompt,
                "timestamp" to System.currentTimeMillis()
            )

            val result = callFunction("generateCodeExample", data)
            result.map { it as Map<String, Any> }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating code example", e)
            Result.failure(e)
        }
    }

    // Analyze user progress and provide recommendations
    suspend fun analyzeProgress(userId: String, agentId: Int, completedTopics: List<String>): Result<Map<String, Any>> {
        return try {
            val data = mapOf(
                "userId" to userId,
                "agentId" to agentId,
                "completedTopics" to completedTopics,
                "timestamp" to System.currentTimeMillis()
            )

            val result = callFunction("analyzeProgress", data)
            result.map { it as Map<String, Any> }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing progress", e)
            Result.failure(e)
        }
    }

    // Generate personalized learning path
    suspend fun generateLearningPath(userId: String, userLevel: String, interests: List<String>): Result<Map<String, Any>> {
        return try {
            val data = mapOf(
                "userId" to userId,
                "userLevel" to userLevel,
                "interests" to interests,
                "timestamp" to System.currentTimeMillis()
            )

            val result = callFunction("generateLearningPath", data)
            result.map { it as Map<String, Any> }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating learning path", e)
            Result.failure(e)
        }
    }

    // Validate and test code submissions
    suspend fun validateCodeSubmission(code: String, language: String, testCases: List<Map<String, Any>>): Result<Map<String, Any>> {
        return try {
            val data = mapOf(
                "code" to code,
                "language" to language,
                "testCases" to testCases,
                "timestamp" to System.currentTimeMillis()
            )

            val result = callFunction("validateCodeSubmission", data)
            result.map { it as Map<String, Any> }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating code submission", e)
            Result.failure(e)
        }
    }

    // Get usage analytics
    suspend fun getUsageAnalytics(userId: String, timeRange: String): Result<Map<String, Any>> {
        return try {
            val data = mapOf(
                "userId" to userId,
                "timeRange" to timeRange,
                "timestamp" to System.currentTimeMillis()
            )

            val result = callFunction("getUsageAnalytics", data)
            result.map { it as Map<String, Any> }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting usage analytics", e)
            Result.failure(e)
        }
    }
}