// Kotlin example for AI Agent Chat
// This demonstrates how to create a simple chat interface

package com.playapp.aiagents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SimpleChatScreen() {
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Messages list
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                Text(
                    text = msg,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message...") }
            )

            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        messages = messages + "You: $message"
                        // Here you would call your AI service
                        messages = messages + "AI: Hello! I received: $message"
                        message = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}