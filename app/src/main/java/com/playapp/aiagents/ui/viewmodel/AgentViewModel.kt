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
        loadAgents()
    }

    private fun loadAgents() {
        viewModelScope.launch {
            repository.getAgents(getApplication()).collect { agentList ->
                _agents.value = agentList
            }
        }
    }
}