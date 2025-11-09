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
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class AgentRepository(private val firebaseService: FirebaseService = FirebaseService()) {

    @OptIn(FlowPreview::class)
    fun getAgents(context: Context): Flow<List<Agent>> = flow {
        println("AgentRepository: getAgents() called")
        // First emit hardcoded agents for testing
        val hardcodedAgents = listOf(
            Agent(
                id = 1,
                title = "LLMs as Operating Systems: Agent Memory",
                provider = "DEEPLEARNING AI",
                instructor = "Joao Moura",
                duration = "2 Hours 41 Minutes",
                description = "Learn how to build multi-agent systems with memory capabilities using CrewAI framework.",
                color = "#6200EE",
                topics = listOf("Multi-Agent Systems", "Memory Management", "CrewAI", "Agent Orchestration"),
                ollamaPrompt = "You are an AI agent expert specialized in multi-agent systems and memory management. Help users understand how agents can maintain context and work together.",
                model = "llama2",
                modelType = "LLAMA2",
                samplePrompts = listOf(
                    "Explain how multi-agent systems maintain memory across conversations",
                    "How does CrewAI handle agent orchestration?",
                    "What are the best practices for agent memory management?"
                ),
                setupInstructions = "This agent uses Llama 2 for multi-agent system discussions. Make sure Ollama is running locally with 'llama2' model installed. The agent specializes in memory management and CrewAI framework.",
                supportsStreaming = true
            ),
            Agent(
                id = 2,
                title = "Foundations of Prompt Engineering (AWS)",
                provider = "AWS SKILLBUILDER",
                instructor = "Amazon Team",
                duration = "4 Hours",
                description = "Master prompt engineering techniques with AWS best practices and real-world examples.",
                color = "#03DAC5",
                topics = listOf("Prompt Design", "AWS Services", "Best Practices", "Optimization"),
                ollamaPrompt = "You are a prompt engineering expert. Guide users on crafting effective prompts and understanding how LLMs interpret instructions.",
                model = "mistral",
                modelType = "MISTRAL",
                samplePrompts = listOf(
                    "How do I write better prompts for AWS services?",
                    "What are the key principles of prompt engineering?",
                    "Give me examples of effective vs ineffective prompts"
                ),
                setupInstructions = "This agent uses Mistral for prompt engineering guidance. Ensure Ollama has 'mistral' model installed. The agent focuses on AWS best practices and prompt optimization techniques.",
                supportsStreaming = true
            ),
            Agent(
                id = 3,
                title = "Introduction to LangGraph",
                provider = "LANGCHAIN ACADEMY",
                instructor = "Harrison Chase",
                duration = "9 Hours",
                description = "Build stateful, graph-based AI agents with LangGraph for complex workflows.",
                color = "#3700B3",
                topics = listOf("LangGraph", "State Management", "Graph Architecture", "Workflows"),
                ollamaPrompt = "You are a LangGraph specialist. Explain how to build graph-based agent workflows and manage complex state transitions.",
                model = "codellama",
                modelType = "CODELLAMA",
                samplePrompts = listOf(
                    "How do I create a simple graph workflow in LangGraph?",
                    "Explain state management in graph-based agents",
                    "What are the advantages of LangGraph over linear chains?"
                ),
                setupInstructions = "This agent uses CodeLlama for code-focused LangGraph discussions. Install 'codellama' model in Ollama. The agent specializes in graph architecture and stateful agent design.",
                supportsStreaming = true
            ),
            Agent(
                id = 4,
                title = "Large Language Model Agents MOOC",
                provider = "AGENTICAI-LEARNING.ORG",
                instructor = "Dawn Song",
                duration = "4 Hours 40 Minutes",
                description = "Comprehensive course on LLM agent fundamentals and advanced capabilities.",
                color = "#BB86FC",
                topics = listOf("LLM Fundamentals", "Agent Design", "Capabilities", "Limitations"),
                ollamaPrompt = "You are an LLM agent educator. Teach core concepts of how language models can act as intelligent agents.",
                model = "llama2",
                modelType = "LLAMA2",
                samplePrompts = listOf(
                    "What are the fundamental capabilities of LLM agents?",
                    "How do LLMs differ from traditional AI agents?",
                    "What are the current limitations of LLM-based agents?"
                ),
                setupInstructions = "This agent uses Llama 2 for educational discussions about LLM agents. Install 'llama2' model in Ollama. The agent covers both fundamentals and advanced agent concepts.",
                supportsStreaming = true
            ),
            Agent(
                id = 5,
                title = "Building AI Agents in LangGraph",
                provider = "DEEPLEARNING AI",
                instructor = "Harrison Chase",
                duration = "1 Hour 32 Minutes",
                description = "Practical guide to implementing AI agents using LangGraph framework.",
                color = "#CF6679",
                topics = listOf("LangGraph Implementation", "Practical Examples", "Agent Patterns", "Integration"),
                ollamaPrompt = "You are a LangGraph implementation expert. Show users how to build practical AI agents with real-world applications.",
                model = "codellama",
                modelType = "CODELLAMA",
                samplePrompts = listOf(
                    "Show me how to implement a simple agent in LangGraph",
                    "What are common agent patterns in LangGraph?",
                    "How do I integrate LangGraph with external APIs?"
                ),
                setupInstructions = "This agent uses CodeLlama for practical LangGraph implementation guidance. Install 'codellama' model in Ollama. The agent focuses on real-world agent building and integration.",
                supportsStreaming = true
            ),
            Agent(
                id = 6,
                title = "Building RAG Agents with LLMs",
                provider = "LEARN NVIDIA",
                instructor = "Nvidia Team",
                duration = "9 Hours",
                description = "Master Retrieval-Augmented Generation (RAG) with NVIDIA's advanced techniques.",
                color = "#03A9F4",
                topics = listOf("RAG Architecture", "Vector Databases", "Retrieval Systems", "NVIDIA Tools"),
                ollamaPrompt = "You are a RAG specialist. Explain how to build retrieval-augmented generation systems for grounded AI responses.",
                model = "mistral",
                modelType = "MISTRAL",
                samplePrompts = listOf(
                    "How does RAG improve LLM responses?",
                    "What vector databases work best with RAG?",
                    "Explain the retrieval process in RAG systems"
                ),
                setupInstructions = "This agent uses Mistral for RAG system discussions. Install 'mistral' model in Ollama. The agent covers NVIDIA tools and advanced RAG techniques.",
                supportsStreaming = true
            ),
            Agent(
                id = 7,
                title = "AI Agentic Design Patterns with AutoGen",
                provider = "DEEPLEARNING AI",
                instructor = "Chi Wang",
                duration = "1 Hour 26 Minutes",
                description = "Explore design patterns for building robust multi-agent systems with AutoGen.",
                color = "#4CAF50",
                topics = listOf("AutoGen Framework", "Design Patterns", "Multi-Agent", "Architecture"),
                ollamaPrompt = "You are an AutoGen expert. Guide users through agentic design patterns and multi-agent orchestration.",
                model = "llama2",
                modelType = "LLAMA2",
                samplePrompts = listOf(
                    "What are the main design patterns in AutoGen?",
                    "How do I orchestrate multiple agents in AutoGen?",
                    "Show me an example of a robust multi-agent system"
                ),
                setupInstructions = "This agent uses Llama 2 for AutoGen framework discussions. Install 'llama2' model in Ollama. The agent specializes in design patterns and multi-agent architecture.",
                supportsStreaming = true
            ),
            Agent(
                id = 8,
                title = "LLMs as Operating Systems: Agent Memory",
                provider = "DEEPLEARNING AI",
                instructor = "Charles Packer",
                duration = "1 Hour 22 Minutes",
                description = "Deep dive into agent memory systems and how LLMs manage context.",
                color = "#FF9800",
                topics = listOf("Memory Systems", "Context Management", "Long-term Memory", "Agent State"),
                ollamaPrompt = "You are an agent memory expert. Explain how AI agents maintain and utilize memory for coherent interactions.",
                model = "neural-chat",
                modelType = "NEURAL_CHAT",
                samplePrompts = listOf(
                    "How do LLMs maintain context in conversations?",
                    "What are different types of agent memory?",
                    "Explain long-term memory in AI agents"
                ),
                setupInstructions = "This agent uses Neural Chat for memory system discussions. Install 'neural-chat' model in Ollama. The agent focuses on context management and memory architectures.",
                supportsStreaming = true
            ),
            Agent(
                id = 9,
                title = "Building Agentic RAG with LlamaIndex",
                provider = "DEEPLEARNING AI",
                instructor = "Jerry Liu",
                duration = "44 Minutes",
                description = "Build advanced RAG systems with agentic capabilities using LlamaIndex.",
                color = "#9C27B0",
                topics = listOf("LlamaIndex", "Agentic RAG", "Query Engines", "Document Processing"),
                ollamaPrompt = "You are a LlamaIndex specialist. Show users how to build intelligent RAG systems with agentic behaviors.",
                model = "mistral",
                modelType = "MISTRAL",
                samplePrompts = listOf(
                    "How do I build agentic RAG with LlamaIndex?",
                    "What are query engines in LlamaIndex?",
                    "Explain document processing in RAG systems"
                ),
                setupInstructions = "This agent uses Mistral for LlamaIndex discussions. Install 'mistral' model in Ollama. The agent covers agentic RAG patterns and advanced query techniques.",
                supportsStreaming = true
            )
        )
        println("AgentRepository: Emitting ${hardcodedAgents.size} hardcoded agents")
        emit(hardcodedAgents)

        // Then try to load from assets
        try {
            val assetAgents = loadAgentsFromAssets(context)
            if (assetAgents.isNotEmpty()) {
                println("AgentRepository: Emitting ${assetAgents.size} agents from assets")
                emit(assetAgents)
            }
        } catch (e: Exception) {
            println("AgentRepository: Failed to load from assets: ${e.message}")
        }

        // Then try Firebase - collect first emission only
        try {
            println("AgentRepository: Attempting to load from Firebase")
            var firebaseAgentsList: List<Agent>? = null
            withTimeout(5000) { // 5 seconds timeout
                firebaseService.getAgents().collect { agents ->
                    if (firebaseAgentsList == null) { // Only take the first emission
                        firebaseAgentsList = agents
                    }
                }
            }

            if (firebaseAgentsList != null && firebaseAgentsList!!.isNotEmpty()) {
                println("AgentRepository: Emitting ${firebaseAgentsList!!.size} agents from Firebase")
                emit(firebaseAgentsList!!)
            } else {
                println("AgentRepository: No agents received from Firebase")
            }
        } catch (e: Exception) {
            println("AgentRepository: Firebase error: ${e.message}")
        }
    }

    private fun loadAgentsFromAssets(context: Context): List<Agent> {
        return try {
            val json = context.assets.open("database.json").bufferedReader().use { it.readText() }
            println("AgentRepository: Loaded JSON from assets, length: ${json.length}")
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(json, type)
            val agentsJson = data["agents"]
            val agents: List<Agent> = if (agentsJson != null) {
                val agentsType = object : TypeToken<List<Agent>>() {}.type
                gson.fromJson(gson.toJson(agentsJson), agentsType)
            } else {
                emptyList()
            }
            println("AgentRepository: Parsed ${agents.size} agents from JSON")
            agents.forEach { agent ->
                println("AgentRepository: Agent ${agent.id}: ${agent.title}")
            }
            agents
        } catch (e: Exception) {
            e.printStackTrace()
            println("AgentRepository: Error loading agents from assets: ${e.message}")
            // Return dummy data if parsing fails
            listOf(
                Agent(1, "Sample Agent", "Provider", "Instructor", "1 Hour", "Description", "#6200EE", listOf("Topic"), "Prompt", "llama2")
            )
        }
    }
}