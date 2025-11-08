package com.playapp.aiagents.ui.playground

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playapp.aiagents.data.repository.ChatRepository
import com.playapp.aiagents.data.service.OllamaApiService
import com.playapp.aiagents.ui.viewmodel.AgentViewModel
import com.playapp.aiagents.data.model.Agent
import com.playapp.aiagents.data.model.OllamaModel
import com.playapp.aiagents.data.model.ChatMessage
import com.playapp.aiagents.data.model.ChatSession
import com.playapp.aiagents.data.repository.AgentRepository
import com.playapp.aiagents.data.repository.ProgressRepository
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.clickable
import com.playapp.aiagents.ui.settings.SettingsActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import com.playapp.aiagents.ui.video.VideoPlayerDialog

class PlaygroundActivity : ComponentActivity() {
    private val viewModel: AgentViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AgentViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AgentViewModel(application, AgentRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private var chatRepository: ChatRepository? = null
    private var ollamaApiService: OllamaApiService? = null
    private var progressRepository: ProgressRepository? = null
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("PlaygroundActivity: onCreate called")

        try {
            val agentId = intent.getIntExtra("agent_id", -1)

            // Debug logging
            println("PlaygroundActivity: Received agent_id = $agentId")

            // Check if ViewModel is initialized
            println("PlaygroundActivity: ViewModel = $viewModel")

            // Initialize services
            println("PlaygroundActivity: Initializing ChatRepository...")
            chatRepository = ChatRepository(this)
            println("PlaygroundActivity: ChatRepository initialized")

            progressRepository = ProgressRepository()
            startTime = System.currentTimeMillis()

            // Get Ollama server URL from preferences
            val prefs = getSharedPreferences("aiagents_settings", Context.MODE_PRIVATE)
            val ollamaServerUrl = prefs.getString("ollama_server_url", "http://10.0.2.2:11434") ?: "http://10.0.2.2:11434"
            println("PlaygroundActivity: Using Ollama server URL: $ollamaServerUrl")
            ollamaApiService = OllamaApiService(baseUrl = ollamaServerUrl)

            // Track agent usage
            lifecycleScope.launch {
                try {
                    progressRepository?.updateAgentProgress("user_123", agentId, agentId,
                        com.playapp.aiagents.data.model.AgentProgress(
                            agentId = agentId,
                            sessionsCount = 1, // Will be incremented properly later
                            lastUsed = System.currentTimeMillis()
                        )
                    )
                    println("PlaygroundActivity: Agent progress updated for agent $agentId")
                } catch (e: Exception) {
                    println("PlaygroundActivity: Error updating agent progress: ${e.message}")
                    // Handle error silently
                }
            }

            println("PlaygroundActivity: About to call setContent")
            setContent {
                println("PlaygroundActivity: setContent called")
                MaterialTheme {
                    println("PlaygroundActivity: MaterialTheme applied")
                    PlaygroundScreen(
                        viewModel = viewModel,
                        agentId = agentId,
                        chatRepository = chatRepository,
                        ollamaApiService = ollamaApiService,
                        progressRepository = progressRepository,
                        onNavigateToSettings = {
                            val intent = Intent(this@PlaygroundActivity, SettingsActivity::class.java)
                            startActivity(intent)
                        },
                        onBackPressed = {
                            finish() // Go back when back button is pressed
                        }
                    )
                }
            }
            println("PlaygroundActivity: setContent completed successfully")
        } catch (e: Exception) {
            println("PlaygroundActivity: Fatal error in onCreate: ${e.message}")
            e.printStackTrace()
            // Show error and finish activity
            android.widget.Toast.makeText(this, "Failed to load playground: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Track time spent in playground
        val timeSpent = System.currentTimeMillis() - startTime
        val agentId = intent.getIntExtra("agent_id", -1)

        lifecycleScope.launch {
            try {
                progressRepository?.addTimeSpent("user_123", agentId, timeSpent)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlaygroundScreen(
    viewModel: AgentViewModel = viewModel(),
    agentId: Int = -1,
    chatRepository: ChatRepository? = null,
    ollamaApiService: OllamaApiService? = null,
    progressRepository: ProgressRepository? = null,
    onNavigateToSettings: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val agents by viewModel.agents.collectAsState()
    val agent = agents.find { it.id == agentId }

    println("PlaygroundScreen: agentId = $agentId, agents.size = ${agents.size}, agent = ${agent?.title ?: "null"}")
    if (agent != null) {
        println("PlaygroundScreen: Found agent: ${agent.title} (id: ${agent.id})")
        println("PlaygroundScreen: Agent modelType: ${agent.modelType}")
        println("PlaygroundScreen: Agent model: ${agent.model}")
    } else {
        println("PlaygroundScreen: Agent not found for id: $agentId")
        // Debug: print all available agents
        agents.forEach { ag ->
            println("PlaygroundScreen: Available agent: ${ag.id} - ${ag.title}")
        }
    }

    // Show loading if agents are not loaded yet, but add a timeout
    if (agents.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading AI Playground...")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Agent ID: $agentId",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "If this takes too long, agents may not be loading properly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Force reload agents
                    println("PlaygroundScreen: Force reloading agents")
                    viewModel.loadAgents()
                }) {
                    Text("Retry Loading")
                }
            }
        }
        return
    }

    // Show error if agent not found and agentId is specified
    if (agentId != -1 && agent == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Agent not found",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "The selected AI agent could not be loaded.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackPressed) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    // All remember calls must be at the top level, outside conditional blocks
    val pagerState = rememberPagerState(pageCount = { 3 })
    val tabTitles = listOf("Learn", "Code", "Chat")

    // Progress state
    var progressPercentage by remember { mutableStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }

    // Chat state
    var currentModel by remember {
        mutableStateOf(
            try {
                agent?.modelType?.let { modelTypeString ->
                    when (modelTypeString.uppercase()) {
                        "LLAMA2" -> OllamaModel.LLAMA2
                        "MISTRAL" -> OllamaModel.MISTRAL
                        "CODELLAMA" -> OllamaModel.CODELLAMA
                        "NEURAL_CHAT" -> OllamaModel.NEURAL_CHAT
                        "VICUNA" -> OllamaModel.VICUNA
                        "ORCA" -> OllamaModel.ORCA
                        else -> OllamaModel.LLAMA2
                    }
                } ?: OllamaModel.LLAMA2
            } catch (e: Exception) {
                println("PlaygroundScreen: Error converting modelType: ${e.message}")
                OllamaModel.LLAMA2
            }
        )
    }
    var isStreaming by remember { mutableStateOf(true) }
    var showModelSelector by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var showSamplePrompts by remember { mutableStateOf(false) }
    var showVideoTutorials by remember { mutableStateOf(false) }
    var currentSession by remember { mutableStateOf<ChatSession?>(null) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var streamingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val listState = rememberLazyListState()

    // Error state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Video player state
    var showVideoPlayer by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf<com.playapp.aiagents.data.model.VideoTutorial?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Initialize session
    LaunchedEffect(agentId) {
        if (agentId != -1 && chatRepository != null) {
            val sessionId = "session_${agentId}_${System.currentTimeMillis()}"
            currentSession = ChatSession(
                id = sessionId,
                agentId = agentId,
                messages = emptyList(),
                createdAt = System.currentTimeMillis()
            )
        }
    }

    // Send message function
    suspend fun sendMessage(message: String, model: OllamaModel, agent: Agent?, chatRepository: ChatRepository?, ollamaApiService: OllamaApiService?, isStreaming: Boolean) {
        if (message.isBlank() || chatRepository == null || ollamaApiService == null || agent == null) return

        isLoading = true
        val sessionId = currentSession?.id ?: return
        val systemPrompt = agent.ollamaPrompt ?: ""

        val userMessage = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            content = message,
            isUser = true,
            timestamp = System.currentTimeMillis(),
            model = model.displayName
        )

        chatRepository!!.addMessage(sessionId, userMessage)
        val updatedMessages = currentSession?.messages?.toMutableList() ?: mutableListOf()
        updatedMessages.add(userMessage)
        currentSession = currentSession?.copy(messages = updatedMessages)

        coroutineScope.launch {
            try {

                if (isStreaming) {
                    var accumulatedResponse = ""
                    val aiMessageId = "ai_${sessionId}_${System.currentTimeMillis()}"
                    
                    ollamaApiService!!.sendChatMessageStreaming(message, model, systemPrompt).collect { (partialResponse, metrics) ->
                        accumulatedResponse += partialResponse
                        
                        if (metrics != null) {
                            // Final response with metrics
                            val aiMessage = ChatMessage(
                                id = aiMessageId,
                                content = accumulatedResponse,
                                isUser = false,
                                timestamp = System.currentTimeMillis(),
                                model = model.displayName
                            )
                            chatRepository!!.addMessageWithMetrics(sessionId, aiMessage, metrics)
                            val finalMessages = currentSession?.messages?.toMutableList() ?: mutableListOf()
                            finalMessages.add(aiMessage)
                            currentSession = currentSession?.copy(messages = finalMessages)
                            streamingMessage = null
                            isLoading = false
                        } else {
                            // Intermediate streaming update - update UI
                            streamingMessage = ChatMessage(
                                id = aiMessageId,
                                content = accumulatedResponse,
                                isUser = false,
                                timestamp = System.currentTimeMillis(),
                                model = model.displayName
                            )
                        }
                    }
                } else {
                    // Handle non-streaming response
                    val (response, metrics) = ollamaApiService!!.sendChatMessage(message, model, systemPrompt)

                    val aiMessage = ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        content = response,
                        isUser = false,
                        timestamp = System.currentTimeMillis(),
                        model = model.displayName
                    )
                    chatRepository!!.addMessageWithMetrics(sessionId, aiMessage, metrics)
                    val nonStreamingMessages = currentSession?.messages?.toMutableList() ?: mutableListOf()
                    nonStreamingMessages.add(aiMessage)
                    currentSession = currentSession?.copy(messages = nonStreamingMessages)
                    isLoading = false
                }

            } catch (e: Exception) {
                println("Error sending message: ${e.message}")
                errorMessage = "Failed to connect to Ollama server. Please check your server URL in Settings. Error: ${e.message}"
                isLoading = false
                // Handle error - could show error message in UI
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Model Switcher
                    IconButton(onClick = { showModelSelector = true }) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = "Switch Model")
                    }

                    // Setup Instructions
                    IconButton(onClick = { showSetupDialog = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Setup")
                    }

                    // Sample Prompts
                    IconButton(onClick = { showSamplePrompts = true }) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = "Sample Prompts")
                    }

                    // Settings
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }

                    // Video Tutorials
                    IconButton(onClick = { showVideoTutorials = true }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Video Tutorials")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Agent Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = agent?.title ?: "AI Playground",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Progress Tracking Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (!isCompleted) {
                    Text(
                        text = "${progressPercentage}% complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Button(
                        onClick = {
                            // Mark as complete logic
                            isCompleted = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Mark as Complete", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> LearnTab(agent)
                    1 -> CodeTab(agent)
                    2 -> ChatTab(
                        agent = agent,
                        currentSession = currentSession,
                        streamingMessage = streamingMessage,
                        messageText = messageText,
                        onMessageTextChange = { messageText = it },
                        isLoading = isLoading,
                        listState = listState,
                        onSendMessage = {
                            coroutineScope.launch {
                                sendMessage(
                                    messageText,
                                    currentModel,
                                    agent,
                                    chatRepository,
                                    ollamaApiService,
                                    isStreaming
                                )
                                messageText = ""
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showModelSelector) {
        ModelSelectorDialog(
            currentModel = currentModel,
            onModelSelected = { currentModel = it },
            onDismiss = { showModelSelector = false }
        )
    }

    if (showSetupDialog) {
        SetupInstructionsDialog(
            agent = agent,
            onDismiss = { showSetupDialog = false }
        )
    }

    if (showSamplePrompts) {
        SamplePromptsDialog(
            prompts = agent?.samplePrompts ?: emptyList(),
            onPromptSelected = { prompt ->
                messageText = prompt
                showSamplePrompts = false
            },
            onDismiss = { showSamplePrompts = false }
        )
    }

    if (showVideoTutorials) {
        VideoTutorialsDialog(
            videos = agent?.videoTutorials ?: emptyList(),
            onVideoSelected = { video ->
                selectedVideo = video
                showVideoPlayer = true
                showVideoTutorials = false
                // Track video watching progress
                coroutineScope.launch {
                    progressRepository?.markVideoWatched("user_123", agentId, video.id)
                }
            },
            onDismiss = { showVideoTutorials = false }
        )
    }

    if (showVideoPlayer && selectedVideo != null) {
        VideoPlayerDialog(
            video = selectedVideo!!,
            onDismiss = {
                showVideoPlayer = false
                selectedVideo = null
            }
        )
    }

    // Error dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Connection Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

// Learn Tab
@Composable
fun LearnTab(agent: Agent?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Course Topics Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.MenuBook,
                            contentDescription = "Course Topics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Course Topics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Topics list
                    agent?.topics?.forEach { topic ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text("• ", color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Text(
                        text = agent?.description ?: "Learn about AI agent development and implementation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            // Video Tutorial Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Video Tutorial",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video Tutorial",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video container placeholder
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.PlayCircle,
                                    contentDescription = "Play Video",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Embedded YouTube Video",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Code Tab
@Composable
fun CodeTab(agent: Agent?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Starter Code Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Starter Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { /* Download code */ },
                            modifier = Modifier.background(
                                color = Color.Yellow.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        ) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = "Download Code",
                                tint = Color.Yellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Code placeholder
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "// Code example for ${agent?.title ?: "AI Agent"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "fun main() {\n    // Your code here\n    println(\"Hello, World!\")\n}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaygroundPreview() {
    MaterialTheme {
        PlaygroundScreen(agentId = 1)
    }
}

// Chat Tab
@Composable
fun ChatTab(
    agent: Agent?,
    currentSession: ChatSession?,
    streamingMessage: ChatMessage?,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSendMessage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Chat header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Chat,
                        contentDescription = "Chat",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Try It with Ollama",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ask this AI agent anything about LLMs operating systems: agent memory powered by Ollama (Local LLM)",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Chat messages area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            currentSession?.messages?.let { messages ->
                items(messages) { message ->
                    ChatMessageBubble(message)
                }
            }

            // Display streaming message if available
            streamingMessage?.let { message ->
                item {
                    ChatMessageBubble(message)
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier.widthIn(max = 280.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI is thinking...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Message input area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about ${agent?.title ?: "AI agents"}...") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message"
                    )
                }
            }
        }
    }
}

// Function for sending messages with actual Ollama API integration

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Text(
                        text = message.model,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ModelSelectorDialog(
    currentModel: OllamaModel,
    onModelSelected: (OllamaModel) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Model", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OllamaModel.values().forEach { model ->
                    TextButton(onClick = { onModelSelected(model); onDismiss() }) {
                        Text(model.displayName)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun SetupInstructionsDialog(
    agent: Agent?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Setup Instructions", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Setup instructions for ${agent?.title ?: "agent"}")
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun SamplePromptsDialog(
    prompts: List<String>,
    onPromptSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sample Prompts", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                prompts.forEach { prompt ->
                    TextButton(onClick = { onPromptSelected(prompt); onDismiss() }) {
                        Text(prompt.take(50) + if (prompt.length > 50) "..." else "")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun VideoTutorialsDialog(
    videos: List<com.playapp.aiagents.data.model.VideoTutorial>,
    onVideoSelected: (com.playapp.aiagents.data.model.VideoTutorial) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Video Tutorials", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                videos.forEach { video ->
                    TextButton(onClick = { onVideoSelected(video); onDismiss() }) {
                        Text(video.title)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}