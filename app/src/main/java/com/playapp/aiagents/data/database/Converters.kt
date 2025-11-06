package com.playapp.aiagents.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.playapp.aiagents.data.model.PerformanceMetrics

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPerformanceMetrics(metrics: PerformanceMetrics?): String? {
        return metrics?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toPerformanceMetrics(json: String?): PerformanceMetrics? {
        return json?.let {
            val type = object : TypeToken<PerformanceMetrics>() {}.type
            gson.fromJson(it, type)
        }
    }
}