package com.playapp.aiagents.data.model

data class UserCourseProgress(
    val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val courseTitle: String = "",
    val progress: Float = 0.0f, // 0.0 to 1.0
    val lastAccessed: String = "",
    val completedAt: String? = null,
    val totalLessons: Int = 0,
    val completedLessons: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)