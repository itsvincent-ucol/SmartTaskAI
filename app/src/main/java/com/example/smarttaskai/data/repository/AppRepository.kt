package com.example.smarttaskai.data.repository

import com.example.smarttaskai.data.dto.AuthResponse
import com.example.smarttaskai.data.dto.LoginRequest
import com.example.smarttaskai.data.dto.RegisterRequest
import com.example.smarttaskai.data.dto.Task
import com.example.smarttaskai.data.service.TaskApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

// Interface keseluruhan untuk aplikasi ini
interface AppRepository {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun getAllTasks(token: String): List<Task>
    suspend fun createTaskManual(token: String, title: RequestBody, description: RequestBody, priority: RequestBody, file: MultipartBody.Part?): Task
    suspend fun analyzeTask(token: String, file: MultipartBody.Part): Task
    suspend fun deleteTask(token: String, id: Long): Response<Unit>

    // Interface untuk update task
    suspend fun updateTitle(token: String, id: Long, title: String): Task
    suspend fun updateDescription(token: String, id: Long, desc: String): Task
    suspend fun updatePriority(token: String, id: Long, prio: String): Task
    suspend fun updateStatus(token: String, id: Long, status: String): Task
    suspend fun updateDueDate(token: String, id: Long, dueDate: String): Task
    suspend fun updateImageUrl(token: String, id: Long, imageUrl: String): Task
}

class NetworkAppRepository(
    private val apiService: TaskApiService
) : AppRepository {
    override suspend fun login(request: LoginRequest): AuthResponse = apiService.login(request)
    override suspend fun register(request: RegisterRequest): AuthResponse = apiService.register(request)
    override suspend fun getAllTasks(token: String): List<Task> = apiService.getAllTasks(token)
    override suspend fun createTaskManual(token: String, title: RequestBody, description: RequestBody, priority: RequestBody, file: MultipartBody.Part?): Task = apiService.createTaskManual(token, title, description, priority, file)
    override suspend fun analyzeTask(token: String, file: MultipartBody.Part): Task = apiService.analyzeTask(token, file)
    override suspend fun deleteTask(token: String, id: Long): Response<Unit> = apiService.deleteTask(token, id)
    override suspend fun updateTitle(token: String, id: Long, title: String): Task = apiService.updateTitle(token, id, title)
    override suspend fun updateDescription(token: String, id: Long, desc: String): Task = apiService.updateDescription(token, id, desc)
    override suspend fun updatePriority(token: String, id: Long, prio: String): Task = apiService.updatePriority(token, id, prio)
    override suspend fun updateStatus(token: String, id: Long, status: String): Task = apiService.updateStatus(token, id, status)
    override suspend fun updateDueDate(token: String, id: Long, dueDate: String): Task = apiService.updateDueDate(token, id, dueDate)
    override suspend fun updateImageUrl(token: String, id: Long, imageUrl: String): Task = apiService.updateImageUrl(token, id, imageUrl)
}