package com.example.SmartTaskAI.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
data class Task(
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String = "", // Added default empty string

    @Column(nullable = false)
    val priority: String = "LOW", // Added default value

    @Column(nullable = false)
    val description: String = "", // Added default empty string

    @Column(name = "image_url")
    val imageUrl: String? = null, // Made nullable with default null

    @Column(name = "created_by", nullable = false)
    val createdBy: String = "System", // Added default value

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(), // Added default value

    @Column(name = "due_date")
    val dueDate: LocalDateTime? = null // Already has default null
)