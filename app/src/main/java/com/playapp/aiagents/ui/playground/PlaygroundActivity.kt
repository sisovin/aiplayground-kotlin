package com.playapp.aiagents.ui.playground

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
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
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
import com.playapp.aiagents.data.model.*
import com.playapp.aiagents.data.repository.AgentRepository
import com.playapp.aiagents.ui.viewmodel.AgentViewModel
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val agentId = intent.getIntExtra("agent_id", -1)

        setContent {
            MaterialTheme {
                PlaygroundScreen(viewModel, agentId) {
                    finish() // Go back when back button is pressed
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(
    viewModel: AgentViewModel = viewModel(),
    agentId: Int = -1,
    onBackPressed: () -> Unit = {}
) {
    val agents by viewModel.agents.collectAsState()
    val agent = agents.find { it.id == agentId }
    val context = LocalContext.current

    var currentModel by remember { mutableStateOf(agent?.modelType ?: OllamaModel.LLAMA2) }
    var isStreaming by remember { mutableStateOf(true) }
    var showModelSelector by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var showSamplePrompts by remember { mutableStateOf(false) }
    var chatSessions by remember { mutableStateOf(listOf<ChatSession>()) }
    var currentSession by remember { mutableStateOf<ChatSession?>(null) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(agent?.title ?: "AI Playground")
                        Text(
                            text = currentModel.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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

                    // Streaming Toggle
                    IconButton(onClick = { isStreaming = !isStreaming }) {
                        Icon(
                            if (isStreaming) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                            contentDescription = "Toggle Streaming",
                            tint = if (isStreaming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat Messages Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentSession?.messages?.isEmpty() == true) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start a conversation with ${agent?.title ?: "this AI agent"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ask questions, get help with ${agent?.topics?.joinToString(", ") ?: "various topics"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                currentSession?.messages?.let { messages ->
                    items(messages) { message ->
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

            // Message Input Area
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
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type your message...") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (messageText.isNotBlank()) {
                                sendMessage(messageText, currentModel)
                                messageText = ""
                            }
                        }),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                sendMessage(messageText, currentModel)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }

    // Model Selector Dialog
    if (showModelSelector) {
        ModelSelectorDialog(
            currentModel = currentModel,
            onModelSelected = { currentModel = it },
            onDismiss = { showModelSelector = false }
        )
    }

    // Setup Instructions Dialog
    if (showSetupDialog) {
        SetupInstructionsDialog(
            agent = agent,
            onDismiss = { showSetupDialog = false }
        )
    }

    // Sample Prompts Dialog
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
}

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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Select AI Model",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OllamaModel.values().forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onModelSelected(model)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = model == currentModel,
                            onClick = {
                                onModelSelected(model)
                                onDismiss()
                            }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = model.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = model.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Setup Instructions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = agent?.setupInstructions ?: """
                        To use this AI agent, you need to:

                        1. Install Ollama from https://ollama.ai
                        2. Pull the required model: `ollama pull ${agent?.model ?: "llama2"}`
                        3. Start Ollama server: `ollama serve`
                        4. Configure your API endpoint in settings
                        5. Test the connection

                        For more information, visit the Ollama documentation.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Sample Prompts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (prompts.isEmpty()) {
                    Text(
                        text = "No sample prompts available for this agent.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(prompts) { prompt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPromptSelected(prompt) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = prompt,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// Mock function for sending messages (replace with actual Ollama API call)
private fun sendMessage(message: String, model: OllamaModel) {
    // TODO: Implement actual Ollama API integration
    // This is a placeholder for the actual implementation
    println("Sending message: $message with model: ${model.displayName}")
}

@Preview(showBackground = true)
@Composable
fun PlaygroundPreview() {
    MaterialTheme {
        PlaygroundScreen(agentId = 1)
    }
}