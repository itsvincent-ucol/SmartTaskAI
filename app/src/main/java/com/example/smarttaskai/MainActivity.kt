package com.example.smarttaskai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smarttaskai.ui.route.AppRouting
import com.example.smarttaskai.ui.theme.SmartTaskAITheme
import com.example.smarttaskai.viewmodel.AuthViewModel
import com.example.smarttaskai.viewmodel.AuthViewModelFactory
import com.example.smarttaskai.viewmodel.DashboardViewModel
import com.example.smarttaskai.viewmodel.DashboardViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appContainer = (application as SmartTaskApplication).container

            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(appContainer.appRepository, appContainer.tokenManager)
            )

            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(appContainer.appRepository, appContainer.tokenManager)
            )

            val navController = rememberNavController()

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