package com.example.SmartTaskAI.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
data class Task(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    var title: String,
    
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    
    var priority: String, // HIGH, MEDIUM, LOW
    
    var createdBy: String? = "ICT Support", // Sementara di-hardcode sebelum ada Login JWT
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    var dueDate: LocalDateTime? = null,
    var imageUrl: String? = null
)