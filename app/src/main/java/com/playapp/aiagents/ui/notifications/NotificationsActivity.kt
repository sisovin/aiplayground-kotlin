package com.playapp.aiagents.ui.notifications

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.playapp.aiagents.ui.viewmodel.AgentViewModel
import com.playapp.aiagents.data.repository.AgentRepository
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : ComponentActivity() {
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
        setContent {
            MaterialTheme {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: AgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackPressed: () -> Unit = {}
) {
    val agents by viewModel.agents.collectAsState()

    // Notification state
    var selectedTab by remember { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(getMockNotifications()) }

    // Filter notifications based on selected tab
    val filteredNotifications = when (selectedTab) {
        0 -> notifications // All
        1 -> notifications.filter { !it.isRead } // Unread
        2 -> notifications.filter { it.type == NotificationType.AI_UPDATE } // AI Updates
        3 -> notifications.filter { it.type == NotificationType.COURSE } // Courses
        else -> notifications
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {
                        // Mark all as read
                        notifications = notifications.map { it.copy(isRead = true) }
                    }) {
                        Icon(Icons.Filled.DoneAll, contentDescription = "Mark all as read")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Unread") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("AI Updates") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Courses") }
                )
            }

            // Content
            if (filteredNotifications.isEmpty()) {
                EmptyNotificationsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredNotifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            onMarkAsRead = { id ->
                                notifications = notifications.map {
                                    if (it.id == id) it.copy(isRead = true) else it
                                }
                            },
                            onDismiss = { id ->
                                notifications = notifications.filter { it.id != id }
                            }
                        )
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        NotificationSettingsDialog(onDismiss = { showSettingsDialog = false })
    }
}

@Composable
fun EmptyNotificationsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "When you have new updates, courses, or AI developments, they'll appear here.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: (String) -> Unit,
    onDismiss: (String) -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDismiss(notification.id)
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.1f))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!notification.isRead) {
                        onMarkAsRead(notification.id)
                    }
                    showDetails = true
                },
            colors = CardDefaults.cardColors(
                containerColor = if (notification.isRead)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            border = if (!notification.isRead) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else null
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Notification Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(notification.type.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        notification.type.icon,
                        contentDescription = null,
                        tint = notification.type.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (notification.isRead)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = notification.timeAgo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )

                    if (notification.actions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            notification.actions.forEach { action ->
                                OutlinedButton(
                                    onClick = { /* Handle action */ },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = action,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Unread indicator
                if (!notification.isRead) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }

    // Details Dialog
    if (showDetails) {
        NotificationDetailsDialog(
            notification = notification,
            onDismiss = { showDetails = false },
            onMarkAsRead = {
                onMarkAsRead(notification.id)
                showDetails = false
            }
        )
    }
}

@Composable
fun NotificationDetailsDialog(
    notification: Notification,
    onDismiss: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.headlineSmall
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(notification.type.color)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Type: ${notification.type.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = notification.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (notification.actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Actions:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    notification.actions.forEach { action ->
                        OutlinedButton(
                            onClick = { /* Handle action */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(action)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onMarkAsRead) {
                Text(if (notification.isRead) "OK" else "Mark as Read")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun NotificationSettingsDialog(onDismiss: () -> Unit) {
    var aiUpdatesEnabled by remember { mutableStateOf(true) }
    var courseUpdatesEnabled by remember { mutableStateOf(true) }
    var systemNotificationsEnabled by remember { mutableStateOf(true) }
    var pushNotificationsEnabled by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Settings") },
        text = {
            Column {
                Text(
                    text = "Choose which notifications you'd like to receive:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Updates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "New AI agents and model updates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = aiUpdatesEnabled,
                        onCheckedChange = { aiUpdatesEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Course Updates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "New courses and learning content",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = courseUpdatesEnabled,
                        onCheckedChange = { courseUpdatesEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "System Notifications",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "App updates and maintenance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = systemNotificationsEnabled,
                        onCheckedChange = { systemNotificationsEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Push Notifications",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Receive notifications on your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { pushNotificationsEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
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

// Data Classes and Types
enum class NotificationType(
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
) {
    AI_UPDATE("AI Update", Icons.Filled.SmartToy, Color(0xFF2196F3)),
    COURSE("Course", Icons.Filled.School, Color(0xFF4CAF50)),
    SYSTEM("System", Icons.Filled.Info, Color(0xFFFF9800)),
    ACHIEVEMENT("Achievement", Icons.Filled.EmojiEvents, Color(0xFFE91E63))
}

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val actions: List<String> = emptyList()
)

// Mock data generator
fun getMockNotifications(): List<Notification> {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val timeAgoFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    return listOf(
        Notification(
            id = "1",
            title = "New AI Agent Available",
            message = "GPT-4 Turbo is now available in the playground! Experience the latest advancements in conversational AI with improved reasoning and creativity.",
            timestamp = dateFormat.format(Date(System.currentTimeMillis() - 3600000)), // 1 hour ago
            timeAgo = "1h ago",
            type = NotificationType.AI_UPDATE,
            isRead = false,
            actions = listOf("Try Now", "Learn More")
        ),
        Notification(
            id = "2",
            title = "Course Completed: Machine Learning Basics",
            message = "Congratulations! You've successfully completed the Machine Learning Basics course. Your certificate is now available in your profile.",
            timestamp = dateFormat.format(Date(System.currentTimeMillis() - 86400000)), // 1 day ago
            timeAgo = "1d ago",
            type = NotificationType.COURSE,
            isRead = false,
            actions = listOf("View Certificate", "Next Course")
        ),
        Notification(
            id = "3",
            title = "Weekly AI Digest",
            message = "This week's top AI developments: New breakthroughs in computer vision, updates to popular language models, and trending research papers.",
            timestamp = dateFormat.format(Date(System.currentTimeMillis() - 172800000)), // 2 days ago
            timeAgo = "2d ago",
            type = NotificationType.AI_UPDATE,
            isRead = true,
            actions = listOf("Read Digest")
        ),
        Notification(
            id = "4",
            title = "Achievement Unlocked: First Steps",
            message = "You've taken your first steps into the world of AI! Keep exploring to unlock more achievements and expand your knowledge.",
            timestamp = dateFormat.format(Date(System.currentTimeMillis() - 259200000)), // 3 days ago
            timeAgo = "3d ago",
            type = NotificationType.ACHIEVEMENT,
            isRead = true,
            actions = listOf("View Achievements")
        ),
        Notification(
            id = "5",
            title = "System Maintenance Complete",
            message = "Scheduled maintenance has been completed. All AI Playground services are now running normally. Thank you for your patience!",
            timestamp = dateFormat.format(Date(System.currentTimeMillis() - 345600000)), // 4 days ago
            timeAgo = "4d ago",
            type = NotificationType.SYSTEM,
            isRead = true
        ),
        Notification(
            id = "6",
            title = "New Course: Advanced Prompt Engineering",
            message = "Master the art of prompt engineering with our new advanced course. Learn techniques used by AI researchers and practitioners.",
            timestamp = dateFormat.format(Date(System.currentTimeMillis() - 432000000)), // 5 days ago
            timeAgo = "5d ago",
            type = NotificationType.COURSE,
            isRead = true,
            actions = listOf("Enroll Now", "Preview Course")
        )
    )
}