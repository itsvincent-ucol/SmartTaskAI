package com.example.smarttaskai.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smarttaskai.data.UiState
import com.example.smarttaskai.ui.view.DashboardScreen
import com.example.smarttaskai.ui.view.LoginScreen
import com.example.smarttaskai.ui.view.RegisterScreen
import com.example.smarttaskai.ui.view.TaskDetailScreen
import com.example.smarttaskai.ui.viewmodel.AuthViewModel
import com.example.smarttaskai.ui.viewmodel.DashboardViewModel
import android.net.Uri
import com.example.smarttaskai.ui.view.CameraScreen
import com.example.smarttaskai.ui.view.CreateTaskScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AppRouting(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel
) {
    // NavHost adalah wadah utama, startDestination diset ke "login" agar aplikasi selalu mulai dari situ.
    NavHost(navController = navController, startDestination = "login") {

        // Ke layar Login
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navController.navigate("dashboard") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        // Ke layar Register
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.navigate("login") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Ke layar dashboard
        composable("dashboard") {
            LaunchedEffect(Unit) {
                dashboardViewModel.fetchTasks()
            }

            DashboardScreen(
                viewModel = dashboardViewModel,
                onTaskClick = { taskId -> navController.navigate("detail/$taskId") },
                onLogoutClick = {
                    navController.navigate("login") { popUpTo("dashboard") { inclusive = true } }
                },
                onCameraClick = { navController.navigate("camera/smart") },
                onAddTaskClick = { navController.navigate("create_task/none") }
            )
        }

        // Ke layar detail task
        composable(
            route = "detail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable

            val savedPhotoUriString = backStackEntry.savedStateHandle.get<String>("edit_photo_uri")
            val tempPhotoUri = savedPhotoUriString?.let { Uri.parse(it) }

            val state by dashboardViewModel.tasksState.collectAsState()
            val task = if (state is UiState.Success) {
                (state as UiState.Success).data.find { it.id == taskId }
            } else null

            if (task != null) {
                TaskDetailScreen(
                    task = task,
                    viewModel = dashboardViewModel,
                    tempPhotoUri = tempPhotoUri,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenCamera = { navController.navigate("camera/edit") }
                )
            }
        }

        // Ke layar camera
        composable(
            route = "camera/{mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "manual"
            val context = LocalContext.current

            CameraScreen(
                mode = mode,
                onCloseCamera = { navController.popBackStack() },
                onPhotoCaptured = { photoUri ->
                    if (mode == "smart") {
                        dashboardViewModel.analyzeAndCreateTask(context, photoUri) {
                            navController.popBackStack()
                        }
                    } else if (mode == "edit") {
                        navController.previousBackStackEntry?.savedStateHandle?.set("edit_photo_uri", photoUri.toString())
                        navController.popBackStack()
                    } else {
                        val encodedUri = Uri.encode(photoUri.toString())
                        navController.navigate("create_task/$encodedUri") {
                            popUpTo("create_task/none") { inclusive = true }
                        }
                    }
                }
            )
        }

        // Ke layar buat task
        composable(
            route = "create_task/{photoUri}",
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("photoUri")
            val photoUri = if (uriString != null && uriString != "none") Uri.parse(Uri.decode(uriString)) else null

            val context = LocalContext.current

            CreateTaskScreen(
                photoUri = photoUri,
                onCancel = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
                onOpenCamera = { navController.navigate("camera/manual") },
                onSubmit = { title, desc, priority ->
                    dashboardViewModel.createTaskManual(
                        context = context,
                        title = title,
                        description = desc,
                        priority = priority,
                        photoUri = photoUri,
                        onSuccess = {
                            navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } }
                        }
                    )
                }
            )
        }

    }
}