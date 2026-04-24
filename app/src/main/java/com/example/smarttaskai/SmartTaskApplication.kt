package com.example.smarttaskai

import android.app.Application
import com.example.smarttaskai.data.container.AppContainer
import com.example.smarttaskai.data.container.DefaultAppContainer

class SmartTaskApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Saat aplikasi baru dibuka, kita langsung menyalakan DefaultAppContainer dimana nanti akan menyiapkan internet (Retrofit) dan penyimpanan token (DataStore) sejak awal.
        container = DefaultAppContainer(this)
    }
}