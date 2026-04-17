package com.example.SmartTaskAI.dto

// Untuk menerima data saat user mendaftar (Register)
data class RegisterRequest(
    val email: String,
    val password: String
)

// Untuk menerima data saat user masuk (Login)
data class LoginRequest(
    val email: String,
    val password: String
)

// Untuk mengirimkan token kembali ke Android setelah sukses
data class AuthResponse(
    val token: String,
    val email: String,
    val role: String
)