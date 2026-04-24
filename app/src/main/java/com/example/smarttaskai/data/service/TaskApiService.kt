package com.example.smarttaskai.data.service

import com.example.smarttaskai.data.dto.AuthResponse
import com.example.smarttaskai.data.dto.LoginRequest
import com.example.smarttaskai.data.dto.RegisterRequest
import com.example.smarttaskai.data.dto.Task
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApiService {

    // Tembak API login ke springboot backend
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // Tembak API register ke springboot backend
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // Tembak API get all task ke springboot backend
    @GET("/api/tasks")
    suspend fun getAllTasks(@Header("Authorization") token: String): List<Task>

    // Tembak API create new task ke springboot backend
    @Multipart
    @POST("/api/tasks/manual")
    suspend fun createTaskManual(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part file: MultipartBody.Part?
    ): Task

    // Tembak API untuk menggunakan Gemini AI ke springboot backend
    @Multipart
    @POST("/api/tasks/analyze")
    suspend fun analyzeTask(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Task

    // Tembak API task secara detail ke springboot backend dan menampilkan detail yang tersimpan di database
    @DELETE("/api/tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>

    // Tembak API detail task dan tampilkan title dari springboot backend
    @PUT("/api/tasks/{id}/title")
    suspend fun updateTitle(@Header("Authorization") token: String, @Path("id") id: Long, @Query("title") title: String): Task

    // Tembak API detail task dan tampilkan description dari springboot backend
    @PUT("/api/tasks/{id}/description")
    suspend fun updateDescription(@Header("Authorization") token: String, @Path("id") id: Long, @Query("description") description: String): Task

    // Tembak API detail task dan tampilkan priority dari springboot backend
    @PUT("/api/tasks/{id}/priority")
    suspend fun updatePriority(@Header("Authorization") token: String, @Path("id") id: Long, @Query("priority") priority: String): Task

    // Tembak API detail task dan tampilkan status dari springboot backend
    @PUT("/api/tasks/{id}/status")
    suspend fun updateStatus(@Header("Authorization") token: String, @Path("id") id: Long, @Query("status") status: String): Task

    // Tembak API detail task dan tampilkan due_date dari springboot backend
    @PUT("/api/tasks/{id}/due_date")
    suspend fun updateDueDate(@Header("Authorization") token: String, @Path("id") id: Long, @Query("due_date") dueDate: String): Task

    // Tembak API untuk hapus task berdasarkan id ke springboot backend
    @PUT("/api/tasks/{id}/image_url")
    suspend fun updateImageUrl(@Header("Authorization") token: String, @Path("id") id: Long, @Query("image_url") imageUrl: String): Task
}