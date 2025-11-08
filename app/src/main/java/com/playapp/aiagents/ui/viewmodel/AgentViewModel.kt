package com.playapp.aiagents.ui.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playapp.aiagents.data.model.Agent
import com.playapp.aiagents.data.repository.AgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AgentViewModel(application: android.app.Application, private val repository: AgentRepository = AgentRepository()) : AndroidViewModel(application) {

    private val _agents = MutableStateFlow<List<Agent>>(emptyList())
    val agents: StateFlow<List<Agent>> = _agents

    init {
        println("AgentViewModel: init called")
        loadAgents()
    }

    fun loadAgents() {
        println("AgentViewModel: loadAgents called")
        viewModelScope.launch {
            try {
                repository.getAgents(getApplication()).collect { agentList ->
                    println("AgentViewModel: Received ${agentList.size} agents")
                    agentList.forEach { agent ->
                        println("AgentViewModel: Agent ${agent.id}: ${agent.title}")
                    }
                    _agents.value = agentList
                    println("AgentViewModel: Updated agents state with ${agentList.size} agents")
                }
            } catch (e: Exception) {
                println("AgentViewModel: Error loading agents: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}