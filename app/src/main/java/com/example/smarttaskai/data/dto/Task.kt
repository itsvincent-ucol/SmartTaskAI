package com.example.smarttaskai.data.dto

data class Task(
    val id: Long,
    val title: String,
    val priority: String,
    val status: String,
    val description: String,
    val imageUrl: String?,
    val createdBy: String?,
    val createdAt: String?,
    val dueDate: String?
)