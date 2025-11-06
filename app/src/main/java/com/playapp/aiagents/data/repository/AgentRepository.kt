package com.playapp.aiagents.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.playapp.aiagents.data.model.Agent
import com.playapp.aiagents.data.service.FirebaseService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.timeout
import kotlin.time.Duration.Companion.seconds

class AgentRepository(private val firebaseService: FirebaseService = FirebaseService()) {

    @OptIn(FlowPreview::class)
    fun getAgents(context: Context): Flow<List<Agent>> = flow {
        // Emit local data immediately to avoid hanging
        emit(loadAgentsFromAssets(context))

        // Then try to fetch from Firebase and emit if available
        try {
            val firebaseAgents = firebaseService.getAgents()
            firebaseAgents.timeout(5.seconds).catch { e ->
                // Firebase failed, but local data already emitted
                e.printStackTrace()
            }.collect { agents ->
                if (agents.isNotEmpty()) {
                    emit(agents)
                }
            }
        } catch (e: Exception) {
            // Firebase failed, but local data already emitted
            e.printStackTrace()
        }
    }

    private fun loadAgentsFromAssets(context: Context): List<Agent> {
        return try {
            val json = context.assets.open("database.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val type = object : TypeToken<Map<String, List<Agent>>>() {}.type
            val data: Map<String, List<Agent>> = gson.fromJson(json, type)
            data["agents"] ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            // Return dummy data if parsing fails
            listOf(
                Agent(1, "Sample Agent", "Provider", "Instructor", "1 Hour", "Description", "#6200EE", listOf("Topic"), "Prompt", "llama2")
            )
        }
    }
}