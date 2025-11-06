package com.playapp.aiagents.data.service

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.playapp.aiagents.data.model.Agent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseService {
    private val TAG = "FirebaseService"
    private val database = FirebaseDatabase.getInstance()
    private val agentsRef = database.getReference("agents")

    // Initialize all Firebase services
    val authService = FirebaseAuthService()
    val storageService = FirebaseStorageService()
    val functionsService = FirebaseFunctionsService()
    // Note: Firebase AI service removed - Firebase Vertex AI not available in standard BoM

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAgents(): Flow<List<Agent>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val agents = snapshot.children.mapNotNull { child ->
                    try {
                        child.getValue(Agent::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to map agent from snapshot: ${e.message}", e)
                        null
                    }
                }
                trySend(agents)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        agentsRef.addValueEventListener(listener)
        awaitClose { agentsRef.removeEventListener(listener) }
    }

    // Enhanced agent data with Firebase integration
    suspend fun getAgentWithResources(agentId: Int): Result<Map<String, Any>> {
        return try {
            // Get agent data from database
            val agentSnapshot = database.getReference("agents").child(agentId.toString()).get().await()
            val agent = agentSnapshot.getValue(Agent::class.java)

            if (agent == null) {
                return Result.failure(Exception("Agent not found"))
            }

            // Get code examples from storage
            val codeExamples = storageService.listFiles("code_examples/$agentId").getOrNull() ?: emptyList()

            // Get video tutorials from storage
            val videoTutorials = storageService.listFiles("video_tutorials/$agentId").getOrNull() ?: emptyList()

            val result = mapOf(
                "agent" to agent,
                "codeExamples" to codeExamples.map { it.name },
                "videoTutorials" to videoTutorials.map { it.name }
            )

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting agent with resources", e)
            Result.failure(e)
        }
    }

    // Upload agent resources
    suspend fun uploadAgentResources(agentId: Int, codeFiles: Map<String, String>, videoFiles: List<String>): Result<Unit> {
        return try {
            // Upload code examples
            for ((fileName, content) in codeFiles) {
                storageService.uploadCodeExample(agentId, fileName, content)
                    .onFailure { Log.e(TAG, "Failed to upload code example $fileName", it) }
            }

            // Upload video tutorials
            for (videoPath in videoFiles) {
                storageService.uploadVideoTutorial(agentId, videoPath)
                    .onFailure { Log.e(TAG, "Failed to upload video tutorial $videoPath", it) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading agent resources", e)
            Result.failure(e)
        }
    }

    // AI-powered chat with agent
    suspend fun chatWithAgent(agentId: Int, message: String, chatHistory: List<Map<String, String>> = emptyList()): Result<String> {
        return try {
            // Get agent data first
            val agentResult = getAgentWithResources(agentId)
            val agent = agentResult.getOrNull()?.get("agent") as? Agent
                ?: return Result.failure(Exception("Agent not found"))

            // For now, return a simple response. Firebase AI would be integrated here
            val response = "Hello! I'm ${agent.title}. I received your message: '$message'. " +
                          "Firebase AI integration would provide intelligent responses here."

            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error chatting with agent", e)
            Result.failure(e)
        }
    }

    // Generate code example for agent
    suspend fun generateCodeExample(agentId: Int, language: String, description: String): Result<String> {
        return try {
            val agentResult = getAgentWithResources(agentId)
            val agent = agentResult.getOrNull()?.get("agent") as? Agent
                ?: return Result.failure(Exception("Agent not found"))

            // For now, return a placeholder. Firebase AI would generate real code here
            val code = when (language.lowercase()) {
                "kotlin" -> """
                    // Kotlin code example for ${agent.title}
                    fun exampleFunction(): String {
                        return "Hello from ${agent.title}!"
                    }
                """.trimIndent()
                "java" -> """
                    // Java code example for ${agent.title}
                    public class Example {
                        public static String exampleMethod() {
                            return "Hello from ${agent.title}!";
                        }
                    }
                """.trimIndent()
                else -> "// Code example for $language - $description"
            }

            Result.success(code)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating code example", e)
            Result.failure(e)
        }
    }

    // Get downloadable code examples
    suspend fun getCodeExamples(agentId: Int): Result<List<Map<String, String>>> {
        return try {
            val files = storageService.listFiles("code_examples/$agentId").getOrNull() ?: emptyList()

            val codeExamples = files.map { fileRef ->
                val downloadUrl = storageService.getDownloadUrl(fileRef.path).getOrNull() ?: ""
                val metadata = storageService.getFileMetadata(fileRef.path).getOrNull()

                mapOf(
                    "name" to fileRef.name,
                    "url" to downloadUrl,
                    "size" to (metadata?.sizeBytes?.toString() ?: "0"),
                    "language" to getLanguageFromFileName(fileRef.name)
                )
            }

            Result.success(codeExamples)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting code examples", e)
            Result.failure(e)
        }
    }

    // Get video tutorials
    suspend fun getVideoTutorials(agentId: Int): Result<List<Map<String, String>>> {
        return try {
            val files = storageService.listFiles("video_tutorials/$agentId").getOrNull() ?: emptyList()

            val videoTutorials = files.map { fileRef ->
                val downloadUrl = storageService.getDownloadUrl(fileRef.path).getOrNull() ?: ""
                val metadata = storageService.getFileMetadata(fileRef.path).getOrNull()

                mapOf(
                    "name" to fileRef.name,
                    "url" to downloadUrl,
                    "size" to (metadata?.sizeBytes?.toString() ?: "0"),
                    "duration" to "0" // Would need additional processing to get video duration
                )
            }

            Result.success(videoTutorials)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting video tutorials", e)
            Result.failure(e)
        }
    }

    private fun getLanguageFromFileName(fileName: String): String {
        return when (fileName.substringAfterLast(".").lowercase()) {
            "kt" -> "Kotlin"
            "java" -> "Java"
            "py" -> "Python"
            "js" -> "JavaScript"
            "ts" -> "TypeScript"
            "cpp", "cc", "cxx" -> "C++"
            "c" -> "C"
            "cs" -> "C#"
            "php" -> "PHP"
            "rb" -> "Ruby"
            "go" -> "Go"
            "rs" -> "Rust"
            "swift" -> "Swift"
            "scala" -> "Scala"
            "sql" -> "SQL"
            else -> "Unknown"
        }
    }
}
