package com.example.smarttaskai.data.container

import android.content.Context
import com.example.smarttaskai.data.local.TokenManager
import com.example.smarttaskai.data.repository.AppRepository
import com.example.smarttaskai.data.repository.NetworkAppRepository
import com.example.smarttaskai.data.service.TaskApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Interface untuk Repository dan TokenManager
interface AppContainer {
    val appRepository: AppRepository
    val tokenManager: TokenManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    // Alamat server saya
    private val baseUrl = "https://apivincent.teamlancer.space/"

    // Inisialisasi pengelola token JWT
    override val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    // sistem logging yang akan muncul disistem Android Studio (UNTUK DEBUG)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor HTTP
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Retrofit untuk mengatus koneksi dengan server dan menerjemahkan JSON jadi Kotlin Object nantinya
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    // Memasang interface API ke Retrofit
    private val retrofitService: TaskApiService by lazy {
        retrofit.create(TaskApiService::class.java)
    }

    // Menghubungkan service Retrofit ke dalam Repository agar dapat digunakan ViewModel.
    override val appRepository: AppRepository by lazy {
        NetworkAppRepository(retrofitService)
    }
}