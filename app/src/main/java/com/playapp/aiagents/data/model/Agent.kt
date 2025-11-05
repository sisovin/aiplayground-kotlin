package com.playapp.aiagents.data.model

/*
 Provide default values so Firebase's DataSnapshot -> object mapping can
 instantiate the class (Firebase requires a no-arg constructor / default values).
*/
data class Agent(
    val id: Int = 0,
    val title: String = "",
    val provider: String = "",
    val instructor: String = "",
    val duration: String = "",
    val description: String = "",
    val color: String = "#FFFFFF", // Color as hex string
    val topics: List<String> = emptyList(),
    val ollamaPrompt: String = "",
    val model: String = ""
)