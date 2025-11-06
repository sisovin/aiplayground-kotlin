package com.playapp.aiagents.ui.courses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playapp.aiagents.R
import com.playapp.aiagents.data.model.Agent
import com.playapp.aiagents.data.repository.AgentRepository
import com.playapp.aiagents.ui.viewmodel.AgentViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star

class CoursesActivity : ComponentActivity() {
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
                CoursesScreen(
                    viewModel = viewModel,
                    onBackPressed = { finish() },
                    onNavigateToCourseDetail = { courseId ->
                        val intent = android.content.Intent(this@CoursesActivity, com.playapp.aiagents.ui.detail.CourseDetailActivity::class.java)
                        intent.putExtra("course_id", courseId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    viewModel: AgentViewModel = viewModel(),
    onBackPressed: () -> Unit = {},
    onNavigateToCourseDetail: (Int) -> Unit = {}
) {
    val agents by viewModel.agents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Courses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(agents) { agent ->
                CourseCard(agent = agent, onClick = { onNavigateToCourseDetail(agent.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCard(agent: Agent, onClick: () -> Unit) {
    val backgroundColor = when (agent.id % 5) {
        0 -> Color(0xFF4CAF50) // Green
        1 -> Color(0xFF2196F3) // Blue
        2 -> Color(0xFFFF9800) // Orange
        3 -> Color(0xFFE91E63) // Pink
        4 -> Color(0xFF9C27B0) // Purple
        else -> Color(0xFF607D8B) // Grey
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Course ${agent.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = agent.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = agent.description.take(100) + if (agent.description.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color.Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "4.${agent.id % 5 + 5}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }

                Text(
                    text = agent.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}