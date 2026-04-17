package com.example.SmartTaskAI.dto

// Menambahkan nilai default agar tidak kacau
data class TaskAIResponse(
    val title: String = "",
    val priority: String = "MEDIUM",
    val description: String = ""
)