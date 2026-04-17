package com.example.SmartTaskAI.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String = "", // Tambahkan = "" agar ada nilai default

    @Column(nullable = false)
    val password: String = "", // Tambahkan = "" agar ada nilai default

    @Column(nullable = false)
    val role: String = "ROLE_ICT"
)