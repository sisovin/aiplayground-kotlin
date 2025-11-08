package com.playapp.aiagents.ui.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Code
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playapp.aiagents.data.model.Agent
import com.playapp.aiagents.data.repository.AgentRepository
import com.playapp.aiagents.data.repository.ProgressRepository
import com.playapp.aiagents.ui.viewmodel.AgentViewModel
import com.playapp.aiagents.ui.video.VideoPlayerDialog
import com.playapp.aiagents.ui.video.openVideoExternally
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class CourseDetailActivity : ComponentActivity() {
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

    private lateinit var progressRepository: ProgressRepository
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val courseId = intent.getIntExtra("course_id", -1)

        progressRepository = ProgressRepository()
        startTime = System.currentTimeMillis()

        // Track that user accessed this course
        lifecycleScope.launch {
            try {
                progressRepository.updateLastAccessed("user_123", courseId) // TODO: Get actual user ID
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }

        setContent {
            MaterialTheme {
                CourseDetailScreen(viewModel, courseId, progressRepository, this@CourseDetailActivity) {
                    finish() // Go back when back button is pressed
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Track time spent on this course
        val timeSpent = System.currentTimeMillis() - startTime
        val courseId = intent.getIntExtra("course_id", -1)

        lifecycleScope.launch {
            try {
                progressRepository.addTimeSpent("user_123", courseId, timeSpent) // TODO: Get actual user ID
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    viewModel: AgentViewModel = viewModel(),
    courseId: Int = -1,
    progressRepository: ProgressRepository? = null,
    activityContext: android.content.Context? = null,
    onBackPressed: () -> Unit = {}
) {
    val agents by viewModel.agents.collectAsState()
    val course = agents.find { it.id == courseId }
    val coroutineScope = rememberCoroutineScope()

    // Video player state
    var showVideoPlayer by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf<com.playapp.aiagents.data.model.VideoTutorial?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Details") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    println("CourseDetailScreen: Start Chat clicked, courseId = $courseId")
                    println("CourseDetailScreen: activityContext = $activityContext")

                    // Check authentication and trial status like bottom navigation
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val isAuthenticated = currentUser != null
                    val isWithinTrial = currentUser?.metadata?.creationTimestamp?.let { creationTime ->
                        val thirtyDaysInMillis: Long = 30L * 24L * 60L * 60L * 1000L // 30 days in milliseconds
                        val currentTime: Long = System.currentTimeMillis()
                        (currentTime - creationTime) <= thirtyDaysInMillis
                    } ?: false

                    activityContext?.let { context ->
                        if (isAuthenticated) {
                            // Authenticated user - start chat with specific agent
                            android.widget.Toast.makeText(context, "Starting chat with agent $courseId", android.widget.Toast.LENGTH_SHORT).show()
                            val intent = android.content.Intent(context, com.playapp.aiagents.ui.playground.PlaygroundActivity::class.java)
                            intent.putExtra("agent_id", courseId)
                            println("CourseDetailScreen: Starting PlaygroundActivity with agent_id = $courseId")
                            context.startActivity(intent)
                        } else if (isWithinTrial) {
                            // Trial user within 30 days - allow direct Playground access
                            android.widget.Toast.makeText(context, "Starting chat with agent $courseId (Trial)", android.widget.Toast.LENGTH_SHORT).show()
                            val intent = android.content.Intent(context, com.playapp.aiagents.ui.playground.PlaygroundActivity::class.java)
                            intent.putExtra("agent_id", courseId)
                            println("CourseDetailScreen: Starting PlaygroundActivity with agent_id = $courseId")
                            context.startActivity(intent)
                        } else {
                            // Not authenticated and trial expired - redirect to sign in
                            android.widget.Toast.makeText(
                                context,
                                "Please sign in to access the AI Playground. Trial users get 30 days free access!",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            val intent = android.content.Intent(context, com.playapp.aiagents.ui.auth.SigninActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                },
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Open Playground") },
                text = { Text("Start Chat") }
            )
        }
    ) { paddingValues ->
        if (course != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Course Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(AndroidColor.parseColor(course.color))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "By ${course.instructor}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = course.provider,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = course.duration,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Course Description
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "About This Course",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                            )
                        }
                    }
                }

                // Course Topics
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Course Topics",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            course.topics.forEachIndexed { index, topic ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.width(24.dp)
                                    )
                                    Text(
                                        text = topic,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Course Model Info
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "AI Model",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = course.model,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Video Tutorials
                if (course.videoTutorials.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Video Tutorials",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                course.videoTutorials.sortedBy { it.order }.forEach { video ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                selectedVideo = video
                                                showVideoPlayer = true
                                                // Mark video as watched
                                                progressRepository?.let { repo ->
                                                    coroutineScope.launch {
                                                        try {
                                                            repo.markVideoWatched("user_123", courseId, video.id)
                                                        } catch (e: Exception) {
                                                            // Handle error silently
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.PlayArrow,
                                                contentDescription = "Play ${video.title}",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = video.title,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "${video.duration} • ${video.description}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (video != course.videoTutorials.last()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Code Examples
                if (course.codeExamples.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Code Examples",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                course.codeExamples.forEach { codeExample ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Code,
                                            contentDescription = "Code",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = codeExample.title,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "${codeExample.language} • ${codeExample.description}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                // TODO: Download code example
                                                progressRepository?.let { repo ->
                                                    coroutineScope.launch {
                                                        try {
                                                            repo.markCodeDownloaded("user_123", courseId)
                                                        } catch (e: Exception) {
                                                            // Handle error silently
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Download,
                                                contentDescription = "Download ${codeExample.title}"
                                            )
                                        }
                                    }
                                    if (codeExample != course.codeExamples.last()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Course not found
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Course Not Found",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The requested course could not be found.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    // Video Player Dialog
    if (showVideoPlayer && selectedVideo != null) {
        VideoPlayerDialog(
            video = selectedVideo!!,
            onDismiss = {
                showVideoPlayer = false
                selectedVideo = null
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailPreview() {
    MaterialTheme {
        CourseDetailScreen(courseId = 1)
    }
}