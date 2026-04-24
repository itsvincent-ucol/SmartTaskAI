package com.example.smarttaskai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarttaskai.data.UiState
import com.example.smarttaskai.data.dto.AuthResponse
import com.example.smarttaskai.data.dto.LoginRequest
import com.example.smarttaskai.data.dto.RegisterRequest
import com.example.smarttaskai.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.example.smarttaskai.data.local.TokenManager

class AuthViewModel(
    private val repository: AppRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // State untuk memantau status login (apakah lagi loading, sukses, atau error).
    private val _loginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<AuthResponse>> = _loginState.asStateFlow()

    // State terpisah untuk registrasi agar pesannya tidak tertukar dengan login.
    private val _registerState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<AuthResponse>> = _registerState.asStateFlow()

    // Proses login dan jika berhasil, token akan disimpan ke dalam TokenManager (DataStore lokal HP).
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val response = repository.login(LoginRequest(email, pass))

                tokenManager.saveToken(response.token)

                _loginState.value = UiState.Success(response)
            } catch (e: HttpException) {
                _loginState.value = UiState.Error("Email atau Password salah")
            } catch (e: Exception) {
                _loginState.value = UiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    // Proses register dan jika sukses, UI akan diarahkan kembali ke layar Login.
    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            try {
                val response = repository.register(RegisterRequest(name, email, pass))
                _registerState.value = UiState.Success(response)
            } catch (e: HttpException) {
                _registerState.value = UiState.Error("Gagal mendaftar. Email mungkin sudah terpakai.")
            } catch (e: IOException) {
                _registerState.value = UiState.Error("Gagal terhubung ke server.")
            } catch (e: Exception) {
                _registerState.value = UiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    // Mengembalikan status UI menjadi Idle (diam)
    fun resetStates() {
        _loginState.value = UiState.Idle
        _registerState.value = UiState.Idle
    }
}

class AuthViewModelFactory(
    private val repository: AppRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}