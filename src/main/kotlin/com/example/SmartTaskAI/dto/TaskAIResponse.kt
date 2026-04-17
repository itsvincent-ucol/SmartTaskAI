package com.example.SmartTaskAI.dto

// Menambahkan nilai default agar Jackson bisa melakukan deserialize (mengubah JSON ke Object)
data class TaskAIResponse(
    val title: String = "",
    val priority: String = "MEDIUM",
    val description: String = ""
)