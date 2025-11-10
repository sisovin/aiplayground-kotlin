package com.playapp.aiagents.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.playapp.aiagents.data.service.FirebaseAuthService
import com.playapp.aiagents.data.service.FirebaseService
import com.playapp.aiagents.data.model.UserSettings
import com.playapp.aiagents.data.model.Theme
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    // Firebase services
    val firebaseService = remember { FirebaseService() }
    val authService = remember { FirebaseAuthService() }

    // Authentication state
    var currentUser by remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(null) }
    var isUserLoggedIn by remember { mutableStateOf(false) }

    // User settings state
    var userSettings by remember { mutableStateOf<UserSettings?>(null) }
    var isLoadingSettings by remember { mutableStateOf(false) }

    // Local settings state (fallback when not logged in)
    var selectedTheme by remember { mutableStateOf(Theme.SYSTEM) }
    var selectedLanguage by remember { mutableStateOf("en") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoSaveConversations by remember { mutableStateOf(true) }
    var defaultModel by remember { mutableStateOf("llama2") }
    var streamingEnabled by remember { mutableStateOf(true) }
    var maxTokens by remember { mutableStateOf(2048) }
    var geminiApiKey by remember { mutableStateOf("") }

    // Ollama settings (local preferences)
    var ollamaServerUrl by remember { mutableStateOf(prefs.getString("ollama_server_url", "http://10.0.2.2:11434") ?: "http://10.0.2.2:11434") }
    var ollamaNetworkExposure by remember { mutableStateOf(prefs.getBoolean("ollama_network_exposure", false)) }
    var modelLocation by remember { mutableStateOf(prefs.getString("model_location", "local") ?: "local") }
    var contextLength by remember { mutableStateOf(prefs.getInt("context_length", 4096)) }
    var airlineMode by remember { mutableStateOf(prefs.getBoolean("airline_mode", false)) }

    // Load authentication state
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            authService.getAuthStateFlow().collect { user ->
                currentUser = user
                isUserLoggedIn = user != null
            }
        }
    }

    // Load user settings when user changes
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // Load user settings from Firebase
            isLoadingSettings = true
            try {
                firebaseService.getUserSettings(currentUser!!.uid).collect { settings ->
                    userSettings = settings
                    if (settings != null) {
                        // Update local state with Firebase settings
                        selectedTheme = settings.theme
                        selectedLanguage = settings.language
                        notificationsEnabled = settings.notificationsEnabled
                        autoSaveConversations = settings.autoSaveConversations
                        defaultModel = settings.defaultModel
                        streamingEnabled = settings.streamingEnabled
                        maxTokens = settings.maxTokens
                        geminiApiKey = settings.geminiApiKey
                    }
                    isLoadingSettings = false
                }
            } catch (e: Exception) {
                Log.e("SettingsActivity", "Error loading user settings", e)
                isLoadingSettings = false
            }
        } else {
            // Load from local preferences when not logged in
            selectedTheme = Theme.valueOf(prefs.getString("theme", "SYSTEM") ?: "SYSTEM")
            selectedLanguage = prefs.getString("language", "en") ?: "en"
            notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
            autoSaveConversations = prefs.getBoolean("auto_save_conversations", true)
            defaultModel = prefs.getString("default_model", "llama2") ?: "llama2"
            streamingEnabled = prefs.getBoolean("streaming_enabled", true)
            maxTokens = prefs.getInt("max_tokens", 2048)
            geminiApiKey = prefs.getString("gemini_api_key", "") ?: ""
            userSettings = null
            isLoadingSettings = false
        }
    }

    // Save settings function
    val saveSettings = {
        coroutineScope.launch {
            if (isUserLoggedIn && currentUser != null) {
                // Save to Firebase
                val settings = UserSettings(
                    id = userSettings?.id ?: "",
                    userId = currentUser!!.uid,
                    theme = selectedTheme,
                    language = selectedLanguage,
                    notificationsEnabled = notificationsEnabled,
                    autoSaveConversations = autoSaveConversations,
                    defaultModel = defaultModel,
                    streamingEnabled = streamingEnabled,
                    maxTokens = maxTokens,
                    geminiApiKey = geminiApiKey,
                    customSettings = userSettings?.customSettings,
                    createdAt = userSettings?.createdAt ?: "",
                    updatedAt = userSettings?.updatedAt ?: ""
                )

                val result = firebaseService.saveUserSettings(currentUser!!.uid, settings)
                if (result.isSuccess) {
                    userSettings = settings
                }
            } else {
                // Save to local preferences
                prefs.edit()
                    .putString("theme", selectedTheme.name)
                    .putString("language", selectedLanguage)
                    .putBoolean("notifications_enabled", notificationsEnabled)
                    .putBoolean("auto_save_conversations", autoSaveConversations)
                    .putString("default_model", defaultModel)
                    .putBoolean("streaming_enabled", streamingEnabled)
                    .putInt("max_tokens", maxTokens)
                    .putString("gemini_api_key", geminiApiKey)
                    .apply()
            }
        }
    }

    // Auto-save when settings change (debounced)
    LaunchedEffect(
        selectedTheme,
        selectedLanguage,
        notificationsEnabled,
        autoSaveConversations,
        defaultModel,
        streamingEnabled,
        maxTokens,
        geminiApiKey
    ) {
        kotlinx.coroutines.delay(500) // Debounce for 500ms
        saveSettings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isLoadingSettings) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            SettingsSection(
                title = "Account",
                icon = Icons.Filled.Person
            ) {
                // Authentication Status
                SettingsItem(
                    title = "Authentication Status",
                    subtitle = if (isUserLoggedIn) {
                        "Signed in as ${currentUser?.email ?: "Unknown"}"
                    } else {
                        "Not signed in"
                    },
                    icon = if (isUserLoggedIn) Icons.Filled.CheckCircle else Icons.Filled.AccountCircle,
                    iconTint = if (isUserLoggedIn) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (isUserLoggedIn) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // User Info
                    SettingsItem(
                        title = currentUser?.displayName ?: "No name set",
                        subtitle = currentUser?.email ?: "",
                        icon = Icons.Filled.Person,
                        showChevron = false
                    )
                }
            }

            // Appearance Section
            SettingsSection(
                title = "Appearance",
                icon = Icons.Filled.Palette
            ) {
                // Theme Selection
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Theme.values().forEach { theme ->
                            FilterChip(
                                selected = selectedTheme == theme,
                                onClick = { selectedTheme = theme },
                                label = { Text(theme.displayName) },
                                leadingIcon = {
                                    Icon(
                                        when (theme) {
                                            Theme.LIGHT -> Icons.Filled.WbSunny
                                            Theme.DARK -> Icons.Filled.NightsStay
                                            Theme.SYSTEM -> Icons.Filled.SettingsBrightness
                                        },
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Language Selection
                SettingsItemWithAction(
                    title = "Language",
                    subtitle = when (selectedLanguage) {
                        "en" -> "English"
                        "es" -> "Español"
                        "fr" -> "Français"
                        "de" -> "Deutsch"
                        else -> selectedLanguage
                    },
                    icon = Icons.Filled.Language
                ) {
                    // Language dropdown would go here
                    // For now, just cycle through available languages
                    val languages = listOf("en", "es", "fr", "de")
                    val currentIndex = languages.indexOf(selectedLanguage)
                    selectedLanguage = languages[(currentIndex + 1) % languages.size]
                }
            }

            // AI Settings Section
            SettingsSection(
                title = "AI Configuration",
                icon = Icons.Filled.SmartToy
            ) {
                // Default Model
                SettingsItemWithAction(
                    title = "Default Model",
                    subtitle = defaultModel,
                    icon = Icons.Filled.ModelTraining
                ) {
                    // Cycle through available models
                    val models = listOf("llama2", "mistral", "codellama", "neural-chat")
                    val currentIndex = models.indexOf(defaultModel)
                    defaultModel = models[(currentIndex + 1) % models.size]
                }

                HorizontalDivider()

                // Max Tokens
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Maximum Tokens",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Controls response length (${maxTokens} tokens)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            maxTokens.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = maxTokens.toFloat(),
                        onValueChange = { maxTokens = it.toInt() },
                        valueRange = 512f..4096f,
                        steps = 7,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("512", style = MaterialTheme.typography.bodySmall)
                        Text("4096", style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider()

                // Streaming Toggle
                SettingsToggle(
                    title = "Enable Streaming",
                    subtitle = "Receive responses in real-time as they're generated",
                    checked = streamingEnabled,
                    onCheckedChange = { streamingEnabled = it },
                    icon = Icons.Filled.Stream
                )

                HorizontalDivider()

                // Gemini API Key
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = { geminiApiKey = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("Enter your Gemini API key") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.Key, contentDescription = null)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Text(
                        "Required for Gemini 2.5 Flash and Gemini 2.5 Pro models when not using local Ollama",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Notifications Section
            SettingsSection(
                title = "Notifications",
                icon = Icons.Filled.Notifications
            ) {
                SettingsToggle(
                    title = "Push Notifications",
                    subtitle = "Receive notifications about new features and updates",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    icon = Icons.Filled.NotificationsActive
                )

                HorizontalDivider()

                SettingsToggle(
                    title = "Auto-save Conversations",
                    subtitle = "Automatically save chat conversations",
                    checked = autoSaveConversations,
                    onCheckedChange = { autoSaveConversations = it },
                    icon = Icons.Filled.Save
                )
            }

            // Ollama Settings Section
            SettingsSection(
                title = "Ollama Configuration",
                icon = Icons.Filled.Settings
            ) {
                // Server URL
                Column(modifier = Modifier.padding(16.dp)) {
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
                        placeholder = { Text("http://localhost:11434") },
                        singleLine = true
                    )
                }

                HorizontalDivider()

                // Network Exposure
                SettingsToggle(
                    title = "Network Exposure",
                    subtitle = "Allow Ollama to be accessed from other devices",
                    checked = ollamaNetworkExposure,
                    onCheckedChange = {
                        ollamaNetworkExposure = it
                        prefs.edit().putBoolean("ollama_network_exposure", it).apply()
                    },
                    icon = Icons.Filled.Wifi
                )

                HorizontalDivider()

                // Model Location
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Model Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
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
                            label = { Text("Local") },
                            leadingIcon = {
                                Icon(Icons.Filled.Computer, contentDescription = null)
                            }
                        )
                        FilterChip(
                            selected = modelLocation == "remote",
                            onClick = {
                                modelLocation = "remote"
                                prefs.edit().putString("model_location", "remote").apply()
                            },
                            label = { Text("Remote") },
                            leadingIcon = {
                                Icon(Icons.Filled.Cloud, contentDescription = null)
                            }
                        )
                    }
                }

                HorizontalDivider()

                // Context Length
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Context Length",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Maximum context tokens (${contextLength})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            contextLength.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = contextLength.toFloat(),
                        onValueChange = {
                            contextLength = it.toInt()
                            prefs.edit().putInt("context_length", it.toInt()).apply()
                        },
                        valueRange = 512f..8192f,
                        steps = 7,
                        modifier = Modifier.padding(top = 8.dp)
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

            // App Settings Section
            SettingsSection(
                title = "App Settings",
                icon = Icons.Filled.AppSettingsAlt
            ) {
                SettingsToggle(
                    title = "Airline Mode",
                    subtitle = "Conserve battery and data usage",
                    checked = airlineMode,
                    onCheckedChange = {
                        airlineMode = it
                        prefs.edit().putBoolean("airline_mode", it).apply()
                    },
                    icon = Icons.Filled.AirplanemodeActive
                )
            }

            // Actions Section
            SettingsSection(
                title = "Actions",
                icon = Icons.Filled.Build
            ) {
                Button(
                    onClick = {
                        // Reset all settings to defaults
                        selectedTheme = Theme.SYSTEM
                        selectedLanguage = "en"
                        notificationsEnabled = true
                        autoSaveConversations = true
                        defaultModel = "llama2"
                        streamingEnabled = true
                        maxTokens = 2048

                        ollamaServerUrl = "http://10.0.2.2:11434"
                        ollamaNetworkExposure = false
                        modelLocation = "local"
                        contextLength = 4096
                        airlineMode = false

                        prefs.edit().clear().apply()
                        saveSettings()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

            // Footer
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "AI Playground v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// Helper Composables for Settings UI

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Section Content
            content()
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showChevron: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showChevron) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsItemWithAction(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}