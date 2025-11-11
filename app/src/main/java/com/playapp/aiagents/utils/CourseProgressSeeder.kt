package com.playapp.aiagents.utils

import android.content.Context
import android.widget.Toast
import com.playapp.aiagents.data.model.UserCourseProgress
import com.playapp.aiagents.data.service.FirebaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility class to populate sample UserCourseProgress data for testing
 */
object CourseProgressSeeder {

    private val firebaseService = FirebaseService()

    /**
     * Seeds sample course progress data for testing
     * Call this from any Activity: CourseProgressSeeder.seedSampleData(context, userId)
     */
    fun seedSampleData(context: Context, userId: String? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                println("CourseProgressSeeder: seedSampleData called with userId=$userId")
                // Use provided userId or get current authenticated user
                val actualUserId = userId ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                println("CourseProgressSeeder: FirebaseAuth currentUser?.uid = ${com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid}")
                println("CourseProgressSeeder: actualUserId resolved to $actualUserId")
                if (actualUserId == null) {
                    val errorMessage = "❌ No authenticated user found. Please sign in first."
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    println(errorMessage)
                    return@launch
                }

                println("🌱 Seeding sample data for user: $actualUserId")

                val sampleCourses = listOf(
                    UserCourseProgress(
                        id = "course_1",
                        userId = actualUserId,
                        courseId = "course1",
                        courseTitle = "LLMs as Operating Systems: Agent Memory",
                        progress = 0.65f,
                        lastAccessed = System.currentTimeMillis().toString(),
                        completedAt = null,
                        totalLessons = 10,
                        completedLessons = 6,
                        createdAt = System.currentTimeMillis().toString(),
                        updatedAt = System.currentTimeMillis().toString()
                    ),
                    UserCourseProgress(
                        id = "course_2",
                        userId = actualUserId,
                        courseId = "course2",
                        courseTitle = "Foundations of Prompt Engineering (AWS)",
                        progress = 0.30f,
                        lastAccessed = (System.currentTimeMillis() - 86400000).toString(), // 1 day ago
                        completedAt = null,
                        totalLessons = 15,
                        completedLessons = 4,
                        createdAt = (System.currentTimeMillis() - 172800000).toString(), // 2 days ago
                        updatedAt = (System.currentTimeMillis() - 86400000).toString()
                    ),
                    UserCourseProgress(
                        id = "course_3",
                        userId = actualUserId,
                        courseId = "course3",
                        courseTitle = "Introduction to LangGraph",
                        progress = 0.85f,
                        lastAccessed = (System.currentTimeMillis() - 3600000).toString(), // 1 hour ago
                        completedAt = null,
                        totalLessons = 12,
                        completedLessons = 10,
                        createdAt = (System.currentTimeMillis() - 259200000).toString(), // 3 days ago
                        updatedAt = (System.currentTimeMillis() - 3600000).toString()
                    )
                )

                var successCount = 0
                var failureCount = 0

                // Save sample data to Firebase
                sampleCourses.forEach { courseProgress ->
                    val result = firebaseService.saveCourseProgress(
                        userId = courseProgress.userId,
                        courseId = courseProgress.courseId,
                        progress = courseProgress
                    )

                    if (result.isSuccess) {
                        successCount++
                    } else {
                        failureCount++
                        println("❌ Failed to save progress for: ${courseProgress.courseTitle} - ${result.exceptionOrNull()?.message}")
                    }
                }

                val message = "✅ Sample data seeding completed!\n" +
                             "📊 Successfully saved: $successCount courses\n" +
                             "❌ Failed: $failureCount courses\n\n" +
                             "� Check Firebase Console:\n" +
                             "https://console.firebase.google.com/project/aiagents-playapp/database"

                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                println(message)

            } catch (e: Exception) {
                val errorMessage = "❌ Error seeding data: ${e.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                println(errorMessage)
                e.printStackTrace()
            }
        }
    }

    /**
     * Test method to verify UserCourseProgress functionality
     * Call this from any Activity: CourseProgressSeeder.testProgressFunctionality(context, userId)
     */
    fun testProgressFunctionality(context: Context, userId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                println("🧪 Testing UserCourseProgress functionality...")

                // Test 1: Get user progress
                firebaseService.getUserCourseProgress(userId).collect { progressList ->
                    println("📊 Retrieved ${progressList.size} course progress records")

                    progressList.forEach { progress ->
                        println("   📚 ${progress.courseTitle}: ${progress.progress * 100}% (${progress.completedLessons}/${progress.totalLessons} lessons)")
                    }

                    val message = "✅ Retrieved ${progressList.size} course progress records"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                val errorMessage = "❌ Error testing progress: ${e.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                println(errorMessage)
                e.printStackTrace()
            }
        }
    }
}