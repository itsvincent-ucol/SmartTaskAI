package com.example.smarttaskai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarttaskai.data.UiState
import com.example.smarttaskai.data.local.TokenManager
import com.example.smarttaskai.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DashboardViewModel(
    private val repository: AppRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _tasksState = MutableStateFlow<UiState<List<Any>>>(UiState.Idle)
    val tasksState: StateFlow<UiState<List<Any>>> = _tasksState.asStateFlow()

    init {
        // Otomatis tarik data saat layar Dashboard dibuka
        fetchTasks()
    }

    fun fetchTasks() {
        viewModelScope.launch {
            _tasksState.value = UiState.Loading

            // 1. Ambil token dari memori HP
            val token = tokenManager.tokenFlow.firstOrNull()

            if (token == null) {
                _tasksState.value = UiState.Error("Sesi Anda habis. Silakan Login ulang.")
                return@launch
            }

            // 2. Tembak API dengan membawa token tersebut
            try {
                // Catatan: Retrofit butuh kata "Bearer " di depan token JWT
                val headerToken = "Bearer $token"
                val response = repository.getAllTasks(headerToken)

                _tasksState.value = UiState.Success(response)
            } catch (e: HttpException) {
                // Jika server menjawab 401 Unauthorized (token kedaluwarsa)
                if (e.code() == 401) {
                    _tasksState.value = UiState.Error("Sesi Anda habis. Silakan Login ulang.")
                    // Hapus token yang rusak/kedaluwarsa
                    tokenManager.clearToken()
                } else {
                    _tasksState.value = UiState.Error("Gagal mengambil data dari server.")
                }
            } catch (e: Exception) {
                _tasksState.value = UiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }
}

// Factory untuk DashboardViewModel
class DashboardViewModelFactory(
    private val repository: AppRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}