package com.example.smarttaskai.data.container

import android.content.Context // Tambahkan import ini
import com.example.smarttaskai.data.local.TokenManager // Tambahkan import ini
import com.example.smarttaskai.data.repository.AppRepository
import com.example.smarttaskai.data.repository.NetworkAppRepository
import com.example.smarttaskai.data.service.TaskApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val appRepository: AppRepository
    val tokenManager: TokenManager // <-- Daftarkan TokenManager di sini
}

// Tambahkan parameter (private val context: Context)
class DefaultAppContainer(private val context: Context) : AppContainer {
    private val baseUrl = "https://apivincent.teamlancer.space/"

    // Inisialisasi TokenManager
    override val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    private val retrofitService: TaskApiService by lazy {
        retrofit.create(TaskApiService::class.java)
    }

    override val appRepository: AppRepository by lazy {
        NetworkAppRepository(retrofitService)
    }
}