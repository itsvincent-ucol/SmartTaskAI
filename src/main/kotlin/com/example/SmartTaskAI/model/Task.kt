package com.example.SmartTaskAI.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalDate // Tambahan wajib untuk tanggal hari ini

@Entity
@Table(name = "tasks")
data class Task(
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String = "",

    @Column(nullable = false)
    val priority: String = "LOW",

    @Column(nullable = false)
    val status: String = "PENDING", 

    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String = "",

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @Column(name = "created_by", nullable = false)
    val createdBy: String = "System",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "due_date")
    val dueDate: LocalDate = LocalDate.now() 
)