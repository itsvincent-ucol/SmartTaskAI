package com.example.smarttaskai

import android.app.Application
import com.example.smarttaskai.data.container.AppContainer
import com.example.smarttaskai.data.container.DefaultAppContainer

class SmartTaskApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Kirim 'this' (Aplikasi ini) sebagai Context ke Container
        container = DefaultAppContainer(this)
    }
}