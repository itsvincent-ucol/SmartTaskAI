package com.example.smarttaskai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smarttaskai.ui.route.AppRouting
import com.example.smarttaskai.ui.theme.SmartTaskAITheme
import com.example.smarttaskai.ui.viewmodel.AuthViewModel
import com.example.smarttaskai.ui.viewmodel.AuthViewModelFactory
import com.example.smarttaskai.ui.viewmodel.DashboardViewModel
import com.example.smarttaskai.ui.viewmodel.DashboardViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Mengambil 'container' dari Application class.
            // Container berisi seperti Retrofit/Repository & TokenManager) yang akan dapat diakses oleh seluruh aplikasi.
            val appContainer = (application as SmartTaskApplication).container

            // Factory perlu digunakan karena ViewModel perlu data dari repository & tokenManager dari container di atas.
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(appContainer.appRepository, appContainer.tokenManager)
            )

            // Membuat DashboardViewModel factory seperti diatas
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(appContainer.appRepository, appContainer.tokenManager)
            )

            val navController = rememberNavController()

            // Membungkus seluruhnya yang nantinya akan dijalankan di App Routing.
            SmartTaskAITheme {
                AppRouting(
                    navController = navController,
                    authViewModel = authViewModel,
                    dashboardViewModel = dashboardViewModel
                )
            }
        }
    }
}