package com.playapp.aiagents.ui.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class CourseDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CourseDetailScreen()
            }
        }
    }
}

@Composable
fun CourseDetailScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Course Detail", style = MaterialTheme.typography.headlineMedium)
        // Add more UI here
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailPreview() {
    MaterialTheme {
        CourseDetailScreen()
    }
}