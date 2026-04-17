package com.example.SmartTaskAI.dto

import java.time.LocalDate // Menggunakan LocalDate, bukan LocalDateTime

data class TaskRequest(
    val title: String,
    val description: String,
    val priority: String = "MEDIUM",
    val dueDate: LocalDate? = null // Otomatis menyesuaikan dengan model database
)