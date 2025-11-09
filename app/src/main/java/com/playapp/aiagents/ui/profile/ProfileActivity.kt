package com.playapp.aiagents.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.playapp.aiagents.ui.viewmodel.AgentViewModel
import com.playapp.aiagents.data.repository.AgentRepository
import com.playapp.aiagents.data.service.FirebaseAuthService
import com.playapp.aiagents.data.service.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import android.net.Uri
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ProfileScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit = {}
) {
    val firebaseService = remember { FirebaseService() }
    val authService = remember { FirebaseAuthService() }
    val coroutineScope = rememberCoroutineScope()

    // Profile state (using hardcoded data for now)
    var showEditDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("AI Explorer") }
    var userEmail by remember { mutableStateOf("explorer@aiplayground.com") }
    var userBio by remember { mutableStateOf("Passionate about exploring the frontiers of AI and machine learning.") }
    var userAvatarUrl by remember { mutableStateOf("") }

    // Calculate stats (mock data for now)
    val totalAgents = 9
    val completedSessions = 15
    val totalTimeSpent = 240 // Mock data in minutes
    val favoriteAgent = "Building Agentic RAG with LlamaIndex"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            item {
                ProfileHeader(
                    userName = userName,
                    userEmail = userEmail,
                    userBio = userBio,
                    userAvatarUrl = userAvatarUrl,
                    onEditClick = { showEditDialog = true }
                )
            }

            // Statistics Section
            item {
                StatisticsSection(
                    totalAgents = totalAgents,
                    completedSessions = completedSessions,
                    totalTimeSpent = totalTimeSpent,
                    favoriteAgent = favoriteAgent
                )
            }

            // Achievements Section
            item {
                AchievementsSection()
            }

            // Recent Activity
            item {
                RecentActivitySection()
            }

            // Account Settings
            item {
                AccountSettingsSection()
            }

            // App Info
            item {
                AppInfoSection()
            }
        }
    }

    // Dialogs
    if (showEditDialog) {
        EditProfileDialog(
            currentName = userName,
            currentEmail = userEmail,
            currentBio = userBio,
            currentAvatarUrl = userAvatarUrl,
            onSave = { name, email, bio, avatarUrl ->
                coroutineScope.launch {
                    val currentUser = authService.currentUser
                    if (currentUser != null) {
                        val userProfile = com.playapp.aiagents.data.model.UserProfile(
                            id = currentUser.uid,
                            fullName = name,
                            email = email,
                            bio = bio,
                            avatarUrl = avatarUrl,
                            createdAt = System.currentTimeMillis().toString(),
                            updatedAt = System.currentTimeMillis().toString()
                        )

                        firebaseService.saveUserProfile(currentUser.uid, userProfile).onSuccess {
                            // Update local state
                            userName = name
                            userEmail = email
                            userBio = bio
                            userAvatarUrl = avatarUrl
                            showEditDialog = false
                            Log.d("ProfileActivity", "Profile saved successfully")
                        }.onFailure { error ->
                            Log.e("ProfileActivity", "Failed to save profile", error)
                            // TODO: Show error message to user
                        }
                    } else {
                        Log.e("ProfileActivity", "No authenticated user found")
                        // TODO: Show error message to user
                    }
                }
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    // Load user profile data from Firebase
    LaunchedEffect(Unit) {
        val currentUser = authService.currentUser
        if (currentUser != null) {
            try {
                firebaseService.getUserProfile(currentUser.uid).collect { profile ->
                    profile?.let {
                        userName = it.fullName
                        userEmail = it.email
                        userBio = it.bio
                        userAvatarUrl = it.avatarUrl
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Failed to load user profile", e)
                // Keep default values if profile loading fails
            }
        } else {
            Log.w("ProfileActivity", "No authenticated user found, using default profile")
            // Keep default values if no user is authenticated
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    userBio: String,
    userAvatarUrl: String,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (userAvatarUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(userAvatarUrl),
                    contentDescription = "Profile Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = userName.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Info
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = userBio,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Edit Button
        OutlinedButton(
            onClick = onEditClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile")
        }
    }
}

@Composable
fun StatisticsSection(
    totalAgents: Int,
    completedSessions: Int,
    totalTimeSpent: Int,
    favoriteAgent: String
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Your Statistics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Filled.SmartToy,
                value = totalAgents.toString(),
                label = "AI Agents Explored",
                modifier = Modifier.weight(1f)
            )

            StatCard(
                icon = Icons.Filled.Chat,
                value = completedSessions.toString(),
                label = "Sessions Completed",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Filled.Schedule,
                value = "${totalTimeSpent}m",
                label = "Time Spent",
                modifier = Modifier.weight(1f)
            )

            StatCard(
                icon = Icons.Filled.Favorite,
                value = favoriteAgent.take(8) + if (favoriteAgent.length > 8) "..." else "",
                label = "Favorite Agent",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AchievementsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val achievements = listOf(
            Achievement("First Steps", "Completed your first AI chat session", Icons.Filled.EmojiEvents, true),
            Achievement("Explorer", "Interacted with 5 different AI agents", Icons.Filled.Explore, true),
            Achievement("Time Traveler", "Spent 2+ hours learning AI", Icons.Filled.History, false),
            Achievement("Master", "Achieved 95%+ accuracy in AI conversations", Icons.Filled.Star, false)
        )

        achievements.forEach { achievement ->
            AchievementCard(achievement)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class Achievement(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val unlocked: Boolean
)

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                achievement.icon,
                contentDescription = null,
                tint = if (achievement.unlocked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.unlocked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (achievement.unlocked) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val activities = listOf(
            "Chatted with AI Agent #1 about machine learning",
            "Completed tutorial on neural networks",
            "Explored advanced chat features",
            "Updated profile preferences"
        )

        activities.forEach { activity ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = activity,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AccountSettingsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Account Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val settings = listOf(
            SettingItem("Privacy Settings", Icons.Filled.Lock, "Manage your privacy preferences"),
            SettingItem("Notification Preferences", Icons.Filled.Notifications, "Configure app notifications"),
            SettingItem("Data & Storage", Icons.Filled.Storage, "Manage your data usage"),
            SettingItem("Help & Support", Icons.Filled.Help, "Get help and contact support")
        )

        settings.forEach { setting ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Handle setting click */ },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        setting.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = setting.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = setting.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class SettingItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String
)

@Composable
fun AppInfoSection() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Playground v1.0.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Built with ❤️ for AI enthusiasts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = { /* Handle terms */ }) {
                Text("Terms of Service")
            }

            TextButton(onClick = { /* Handle privacy */ }) {
                Text("Privacy Policy")
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    currentBio: String,
    currentAvatarUrl: String,
    onSave: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var bio by remember { mutableStateOf(currentBio) }
    var avatarUrl by remember { mutableStateOf(currentAvatarUrl) }
    var isUploading by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // For now, just set the URI as string (in a real app, you'd upload to storage)
            avatarUrl = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                // Avatar Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(avatarUrl),
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = fullName.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Profile Picture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            enabled = !isUploading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isUploading) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isUploading) "Uploading..." else "Change Photo")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(fullName, email, bio, avatarUrl)
                },
                enabled = !isUploading
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column {
                Text("App settings and preferences will be available here.")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Coming soon...", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}