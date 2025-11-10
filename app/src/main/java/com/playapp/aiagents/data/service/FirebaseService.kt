package com.playapp.aiagents.data.service

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.playapp.aiagents.data.model.Agent
import com.playapp.aiagents.data.model.Banner
import com.playapp.aiagents.data.model.UserSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseService {
    private val TAG = "FirebaseService"
    private val database = FirebaseDatabase.getInstance()
    private val agentsRef = database.getReference("agents")
    private val bannersRef = database.getReference("Banner")

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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getBanners(): Flow<List<Banner>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Firebase banners snapshot: exists=${snapshot.exists()}, hasChildren=${snapshot.hasChildren()}, value=${snapshot.value}")

                val banners = if (snapshot.hasChildren()) {
                    // Data stored as individual children (preferred format)
                    Log.d(TAG, "Parsing banners as children")
                    snapshot.children.mapNotNull { child ->
                        try {
                            val banner = child.getValue(Banner::class.java)
                            Log.d(TAG, "Parsed banner from child: $banner")
                            banner
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to map banner from snapshot: ${e.message}", e)
                            null
                        }
                    }
                } else if (snapshot.exists() && snapshot.value != null) {
                    // Data stored as array - try to parse as List<Banner>
                    Log.d(TAG, "Parsing banners as array, value type: ${snapshot.value?.javaClass}")
                    try {
                        val bannerList = snapshot.getValue(object : GenericTypeIndicator<List<Banner>>() {})
                        Log.d(TAG, "Parsed banner array: ${bannerList?.size} items")
                        bannerList ?: emptyList()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse banner array: ${e.message}", e)
                        // Try to cast directly
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val value = snapshot.value as? List<Map<String, Any>>
                            Log.d(TAG, "Trying direct cast, value: $value")
                            value?.mapNotNull { map ->
                                try {
                                    Banner(url = map["url"] as? String ?: "")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to create banner from map: $e")
                                    null
                                }
                            } ?: emptyList()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed direct cast: ${e.message}", e)
                            emptyList()
                        }
                    }
                } else {
                    Log.d(TAG, "No banner data in Firebase")
                    emptyList()
                }

                Log.d(TAG, "Final banner list size: ${banners.size}")
                trySend(banners)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase banners listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        bannersRef.addValueEventListener(listener)
        awaitClose { bannersRef.removeEventListener(listener) }
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

    // Profile management
    private val profilesRef = database.getReference("profiles")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserProfile(userId: String): Flow<com.playapp.aiagents.data.model.UserProfile?> = callbackFlow {
        val profileRef = profilesRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(com.playapp.aiagents.data.model.UserProfile::class.java)
                trySend(profile)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        profileRef.addValueEventListener(listener)
        awaitClose { profileRef.removeEventListener(listener) }
    }

    suspend fun saveUserProfile(userId: String, profile: com.playapp.aiagents.data.model.UserProfile): Result<Unit> {
        return try {
            val profileRef = profilesRef.child(userId)
            profileRef.setValue(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val profileRef = profilesRef.child(userId)
            profileRef.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    // User settings management
    private val userSettingsRef = database.getReference("user_settings")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserSettings(userId: String): Flow<UserSettings?> = callbackFlow {
        val settingsRef = userSettingsRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val settings = snapshot.getValue(UserSettings::class.java)
                trySend(settings)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        settingsRef.addValueEventListener(listener)
        awaitClose { settingsRef.removeEventListener(listener) }
    }

    suspend fun saveUserSettings(userId: String, settings: UserSettings): Result<Unit> {
        return try {
            val settingsRef = userSettingsRef.child(userId)
            settingsRef.setValue(settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user settings", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserSettings(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val settingsRef = userSettingsRef.child(userId)
            settingsRef.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user settings", e)
            Result.failure(e)
        }
    }

    // Course progress management
    private val progressRef = database.getReference("course_progress")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserCourseProgress(userId: String): Flow<List<com.playapp.aiagents.data.model.UserCourseProgress>> = callbackFlow {
        val userProgressRef = progressRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progressList = snapshot.children.mapNotNull { child ->
                    try {
                        child.getValue(com.playapp.aiagents.data.model.UserCourseProgress::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to map user course progress from snapshot: ${e.message}", e)
                        null
                    }
                }
                trySend(progressList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        userProgressRef.addValueEventListener(listener)
        awaitClose { userProgressRef.removeEventListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCourseProgress(userId: String, courseId: String): Flow<com.playapp.aiagents.data.model.UserCourseProgress?> = callbackFlow {
        val courseProgressRef = progressRef.child(userId).child(courseId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progress = snapshot.getValue(com.playapp.aiagents.data.model.UserCourseProgress::class.java)
                trySend(progress)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        courseProgressRef.addValueEventListener(listener)
        awaitClose { courseProgressRef.removeEventListener(listener) }
    }

    suspend fun saveCourseProgress(userId: String, courseId: String, progress: com.playapp.aiagents.data.model.UserCourseProgress): Result<Unit> {
        return try {
            val courseProgressRef = progressRef.child(userId).child(courseId)
            courseProgressRef.setValue(progress).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving course progress", e)
            Result.failure(e)
        }
    }

    suspend fun updateCourseProgress(userId: String, courseId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val courseProgressRef = progressRef.child(userId).child(courseId)
            courseProgressRef.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating course progress", e)
            Result.failure(e)
        }
    }

    suspend fun updateProgressOnLessonComplete(userId: String, courseId: String, courseTitle: String, totalLessons: Int): Result<Unit> {
        return try {
            val courseProgressRef = progressRef.child(userId).child(courseId)

            // Get current progress
            val currentSnapshot = courseProgressRef.get().await()
            val currentProgress = currentSnapshot.getValue(com.playapp.aiagents.data.model.UserCourseProgress::class.java)

            val now = System.currentTimeMillis().toString()
            val completedLessons = (currentProgress?.completedLessons ?: 0) + 1
            val progress = completedLessons.toFloat() / totalLessons.toFloat()

            val updatedProgress = com.playapp.aiagents.data.model.UserCourseProgress(
                id = courseId,
                userId = userId,
                courseId = courseId,
                courseTitle = courseTitle,
                progress = progress,
                lastAccessed = now,
                completedAt = if (progress >= 1.0f) now else null,
                totalLessons = totalLessons,
                completedLessons = completedLessons,
                createdAt = currentProgress?.createdAt ?: now,
                updatedAt = now
            )

            courseProgressRef.setValue(updatedProgress).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress on lesson complete", e)
            Result.failure(e)
        }
    }
}
