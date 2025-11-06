package com.playapp.aiagents.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.playapp.aiagents.data.model.UserProgress
import com.playapp.aiagents.data.model.CourseProgress
import com.playapp.aiagents.data.model.AgentProgress
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProgressRepository {
    private val database = FirebaseDatabase.getInstance()
    private val progressRef = database.getReference("user_progress")

    companion object {
        private const val TAG = "ProgressRepository"
    }

    /**
     * Get user progress for a specific user
     */
    fun getUserProgress(userId: String): Flow<UserProgress> = callbackFlow {
        val userProgressRef = progressRef.child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val progress = snapshot.getValue(UserProgress::class.java) ?: UserProgress(userId = userId)
                    trySend(progress)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user progress", e)
                    trySend(UserProgress(userId = userId))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error getting user progress", error.toException())
                trySend(UserProgress(userId = userId))
            }
        }

        userProgressRef.addValueEventListener(listener)

        awaitClose {
            userProgressRef.removeEventListener(listener)
        }
    }

    /**
     * Update user progress
     */
    suspend fun updateUserProgress(userId: String, progress: UserProgress) {
        try {
            progressRef.child(userId).setValue(progress).await()
            Log.d(TAG, "User progress updated for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user progress", e)
            throw e
        }
    }

    /**
     * Update course progress for a user
     */
    suspend fun updateCourseProgress(userId: String, courseId: Int, courseProgress: CourseProgress) {
        try {
            val userProgressRef = progressRef.child(userId)
            val coursesRef = userProgressRef.child("courses").child(courseId.toString())

            coursesRef.setValue(courseProgress).await()

            // Update last active timestamp
            userProgressRef.child("lastActive").setValue(System.currentTimeMillis()).await()

            Log.d(TAG, "Course progress updated for user: $userId, course: $courseId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating course progress", e)
            throw e
        }
    }

    /**
     * Update agent progress within a course
     */
    suspend fun updateAgentProgress(userId: String, courseId: Int, agentId: Int, agentProgress: AgentProgress) {
        try {
            val agentRef = progressRef
                .child(userId)
                .child("courses")
                .child(courseId.toString())
                .child("agentProgress")
                .child(agentId.toString())

            agentRef.setValue(agentProgress).await()
            Log.d(TAG, "Agent progress updated for user: $userId, course: $courseId, agent: $agentId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating agent progress", e)
            throw e
        }
    }

    /**
     * Mark code as downloaded for a course
     */
    suspend fun markCodeDownloaded(userId: String, courseId: Int) {
        try {
            progressRef
                .child(userId)
                .child("courses")
                .child(courseId.toString())
                .child("downloadedCode")
                .setValue(true)
                .await()

            Log.d(TAG, "Code download marked for user: $userId, course: $courseId")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking code download", e)
            throw e
        }
    }

    /**
     * Mark video as watched for a course
     */
    suspend fun markVideoWatched(userId: String, courseId: Int, videoId: String) {
        try {
            val videosRef = progressRef
                .child(userId)
                .child("courses")
                .child(courseId.toString())
                .child("watchedVideos")

            // Get current watched videos
            val currentVideos = videosRef.get().await().getValue(object : com.google.firebase.database.GenericTypeIndicator<List<String>>() {}) ?: emptyList()

            // Add new video if not already present
            if (!currentVideos.contains(videoId)) {
                val updatedVideos = currentVideos + videoId
                videosRef.setValue(updatedVideos).await()
                Log.d(TAG, "Video marked as watched for user: $userId, course: $courseId, video: $videoId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking video watched", e)
            throw e
        }
    }

    /**
     * Add time spent to course progress
     */
    suspend fun addTimeSpent(userId: String, courseId: Int, timeSpent: Long) {
        try {
            val courseRef = progressRef
                .child(userId)
                .child("courses")
                .child(courseId.toString())

            // Get current time spent
            val currentTime = courseRef.child("timeSpent").get().await().getValue(Long::class.java) ?: 0L
            val newTime = currentTime + timeSpent

            courseRef.child("timeSpent").setValue(newTime).await()

            // Update total time spent for user
            val userRef = progressRef.child(userId)
            val currentTotalTime = userRef.child("totalTimeSpent").get().await().getValue(Long::class.java) ?: 0L
            val newTotalTime = currentTotalTime + timeSpent
            userRef.child("totalTimeSpent").setValue(newTotalTime).await()

            Log.d(TAG, "Time spent updated for user: $userId, course: $courseId, added: $timeSpent ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating time spent", e)
            throw e
        }
    }

    /**
     * Update last accessed time for a course
     */
    suspend fun updateLastAccessed(userId: String, courseId: Int) {
        try {
            val timestamp = System.currentTimeMillis()
            progressRef
                .child(userId)
                .child("courses")
                .child(courseId.toString())
                .child("lastAccessed")
                .setValue(timestamp)
                .await()

            progressRef
                .child(userId)
                .child("lastActive")
                .setValue(timestamp)
                .await()

            Log.d(TAG, "Last accessed updated for user: $userId, course: $courseId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last accessed", e)
            throw e
        }
    }
}