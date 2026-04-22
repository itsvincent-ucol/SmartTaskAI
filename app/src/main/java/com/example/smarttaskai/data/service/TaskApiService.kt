package com.example.smarttaskai.data.service

import com.example.smarttaskai.data.dto.AuthResponse
import com.example.smarttaskai.data.dto.LoginRequest
import com.example.smarttaskai.data.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TaskApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // PERBAIKAN DI SINI: Tambahkan ini untuk Dashboard
    @GET("/api/tasks")
    suspend fun getAllTasks(@Header("Authorization") token: String): List<Any>
}