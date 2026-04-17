package com.example.SmartTaskAI.dto

// Pastikan property-nya persis dengan format JSON yang akan kita minta dari AI
data class TaskAIResponse(
    val title: String,
    val priority: String,
    val description: String
)