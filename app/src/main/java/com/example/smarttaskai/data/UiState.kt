package com.example.smarttaskai.data

sealed class UiState<out T> {

    // Sedang diam (belum ada proses API yang jalan). Tampilan layar normal.
    object Idle : UiState<Nothing>()

    // API sedang memproses. UI biasanya merespon dengan nge-draw gambar muter (Loading spinner).
    object Loading : UiState<Nothing>()

    // Berhasil. Bawa return data (misalnya List<Task>) dari server untuk dirender/digambar ke layar.
    data class Success<T>(val data: T) : UiState<T>()

    // Gagal. Bawa return error (misal "Password salah") untuk ditampilkan dengan warna merak.
    data class Error(val message: String) : UiState<Nothing>()
}