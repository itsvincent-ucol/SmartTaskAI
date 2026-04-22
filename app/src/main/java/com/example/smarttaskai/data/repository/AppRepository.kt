package com.example.smarttaskai.data.repository

import com.example.smarttaskai.data.dto.AuthResponse
import com.example.smarttaskai.data.dto.LoginRequest
import com.example.smarttaskai.data.dto.RegisterRequest
import com.example.smarttaskai.data.service.TaskApiService

interface AppRepository {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    // Tambahkan kontrak baru
    suspend fun getAllTasks(token: String): List<Any>
}

class NetworkAppRepository(
    private val apiService: TaskApiService
) : AppRepository {

    override suspend fun login(request: LoginRequest): AuthResponse {
        return apiService.login(request)
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        return apiService.register(request)
    }

    // Tambahkan implementasi untuk memanggil API Task
    override suspend fun getAllTasks(token: String): List<Any> {
        return apiService.getAllTasks(token)
    }
}