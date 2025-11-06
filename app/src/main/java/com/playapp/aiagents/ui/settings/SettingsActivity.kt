package com.playapp.aiagents.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playapp.aiagents.ui.viewmodel.AgentViewModel

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(onBackPressed = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("aiagents_settings", Context.MODE_PRIVATE) }

    // Settings state
    var userName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var userEmail by remember { mutableStateOf(prefs.getString("user_email", "") ?: "") }
    var ollamaServerUrl by remember { mutableStateOf(prefs.getString("ollama_server_url", "http://10.0.2.2:11434") ?: "http://10.0.2.2:11434") }
    var ollamaNetworkExposure by remember { mutableStateOf(prefs.getBoolean("ollama_network_exposure", false)) }
    var modelLocation by remember { mutableStateOf(prefs.getString("model_location", "local") ?: "local") }
    var contextLength by remember { mutableStateOf(prefs.getInt("context_length", 4096)) }
    var airlineMode by remember { mutableStateOf(prefs.getBoolean("airline_mode", false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // User Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "User Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = userName,
                        onValueChange = {
                            userName = it
                            prefs.edit().putString("user_name", it).apply()
                        },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null)
                        }
                    )

                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = {
                            userEmail = it
                            prefs.edit().putString("user_email", it).apply()
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null)
                        }
                    )
                }
            }

            // Ollama Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Ollama Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Ollama Server URL
                    OutlinedTextField(
                        value = ollamaServerUrl,
                        onValueChange = {
                            ollamaServerUrl = it
                            prefs.edit().putString("ollama_server_url", it).apply()
                        },
                        label = { Text("Ollama Server URL") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.Link, contentDescription = null)
                        },
                        placeholder = { Text("http://localhost:11434") }
                    )

                    HorizontalDivider()

                    // Network Exposure Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Network Exposure",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Allow Ollama to be accessed from other devices on the network",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = ollamaNetworkExposure,
                            onCheckedChange = {
                                ollamaNetworkExposure = it
                                prefs.edit().putBoolean("ollama_network_exposure", it).apply()
                            }
                        )
                    }

                    HorizontalDivider()

                    // Model Location
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Model Location",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = modelLocation == "local",
                                onClick = {
                                    modelLocation = "local"
                                    prefs.edit().putString("model_location", "local").apply()
                                },
                                label = { Text("Local") }
                            )
                            FilterChip(
                                selected = modelLocation == "remote",
                                onClick = {
                                    modelLocation = "remote"
                                    prefs.edit().putString("model_location", "remote").apply()
                                },
                                label = { Text("Remote") }
                            )
                        }
                    }

                    HorizontalDivider()

                    // Context Length
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Context Length",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Maximum tokens to keep in context (${contextLength})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = contextLength.toFloat(),
                            onValueChange = {
                                contextLength = it.toInt()
                                prefs.edit().putInt("context_length", it.toInt()).apply()
                            },
                            valueRange = 512f..8192f,
                            steps = 7
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("512", style = MaterialTheme.typography.bodySmall)
                            Text("8192", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // App Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.AppSettingsAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "App Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Airline Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Airline Mode",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Conserve battery and data usage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = airlineMode,
                            onCheckedChange = {
                                airlineMode = it
                                prefs.edit().putBoolean("airline_mode", it).apply()
                            }
                        )
                    }

                    HorizontalDivider()

                    // Reset Settings
                    Button(
                        onClick = {
                            // Reset all settings to defaults
                            userName = ""
                            userEmail = ""
                            ollamaNetworkExposure = false
                            modelLocation = "local"
                            contextLength = 4096
                            airlineMode = false

                            prefs.edit().clear().apply()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Filled.Restore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Settings")
                    }
                }
            }
        }
    }
}