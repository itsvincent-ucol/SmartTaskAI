package com.example.smarttaskai.ui.route

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smarttaskai.view.LoginScreen
import com.example.smarttaskai.view.RegisterScreen
import com.example.smarttaskai.viewmodel.AuthViewModel
import com.example.smarttaskai.view.DashboardScreen
import com.example.smarttaskai.viewmodel.DashboardViewModel

@Composable
fun AppRouting(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel // PERBAIKAN DI SINI: Tambahkan parameter ini
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navController.navigate("dashboard") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.navigate("login") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                viewModel = dashboardViewModel, // Sekarang ini tidak akan error lagi merah
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}