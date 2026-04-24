package com.example.smarttaskai.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarttaskai.data.UiState
import com.example.smarttaskai.data.local.TokenManager
import com.example.smarttaskai.data.repository.AppRepository
import com.example.smarttaskai.data.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.delay

class DashboardViewModel(
    private val repository: AppRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Menyimpan state dari daftar tugas (Idle, Loading, Success, Error).
    private val _tasksState = MutableStateFlow<UiState<List<Task>>>(UiState.Idle)

    // Menyimpan state apakah AI sedang menganalisa foto atau tidak (untuk nampilin animasi loading).
    val tasksState: StateFlow<UiState<List<Task>>> = _tasksState.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    init {
        // Otomatis tarik data tugas begitu ViewModel ini dibuat.
        fetchTasks()
    }

    // Fungsi utama untuk mengambil daftar tugas dari server (menampilkan animasi loading).
    fun fetchTasks() {
        viewModelScope.launch {
            _tasksState.value = UiState.Loading
            val token = tokenManager.tokenFlow.firstOrNull()

            // Jika token tidak ada (belum login), kembalikan error.
            if (token == null) {
                _tasksState.value = UiState.Error("Sesi Anda habis. Silakan Login ulang.")
                return@launch
            }

            try {
                // Memanggil repository dan  akan menembak TaskApiService.kt)
                val headerToken = "Bearer $token"
                val response = repository.getAllTasks(headerToken)
                _tasksState.value = UiState.Success(response)
            } catch (e: HttpException) {
                // Jika error 401 (Unauthorized), artinya token expired. Hapus token dan minta login lagi.
                if (e.code() == 401) {
                    _tasksState.value = UiState.Error("Sesi Anda habis. Silakan Login ulang.")
                    tokenManager.clearToken()
                } else {
                    _tasksState.value = UiState.Error("Gagal mengambil data dari server.")
                }
            } catch (e: Exception) {
                _tasksState.value = UiState.Error("Terjadi kesalahan, ${e.message}")
            }
        }
    }

    // Fungsi untuk get data ulang di balik layar
    fun refreshTasks() {
        viewModelScope.launch {
            val token = tokenManager.tokenFlow.firstOrNull() ?: return@launch
            try {
                val tasks = repository.getAllTasks("Bearer $token")
                _tasksState.value = UiState.Success(tasks)
            } catch (e: Exception) {
                Log.e("Dashboard", "Gagal refresh: ${e.message}")
            }
        }
    }

    // Fungsi logout user dan menghapus token dari memori HP.
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }

    // Fungsi untuk create task secara manual
    fun createTaskManual(
        context: Context, title: String, description: String, priority: String, photoUri: Uri?, onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _tasksState.value = UiState.Loading
            val token = tokenManager.tokenFlow.firstOrNull() ?: return@launch

            try {
                // Konversi tipe data String menjadi RequestBody agar bisa dikirim via form-data Retrofit
                val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val priorityBody = priority.toRequestBody("text/plain".toMediaTypeOrNull())

                // Jika ada foto, ubah Uri dari HP menjadi Multipart file.
                val imagePart = photoUri?.let { uri ->
                    val file = uriToFile(context, uri)
                    val requestFile = file?.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", file?.name, requestFile!!)
                }

                // Tembak ke backend dan setelah berhasil, tarik data terbaru (fetchTasks) lalu jalankan onSuccess (biasanya untuk navigasi).
                repository.createTaskManual("Bearer $token", titleBody, descBody, priorityBody, imagePart)
                fetchTasks()
                onSuccess()
            } catch (e: Exception) {
                _tasksState.value = UiState.Error("Gagal simpan: ${e.message}")
            }
        }
    }

    // Fungsi untuk menganalisa foto menggunakan AI.
    fun analyzeAndCreateTask(context: Context, photoUri: Uri, onSuccess: () -> Unit) {
        // Langsung pindah layar dulu agar user tidak menunggu di layar kamera.
        onSuccess()

        viewModelScope.launch {
            // Nyalakan info "Analyzing..." di Dashboard.
            _isAnalyzing.value = true
            val token = tokenManager.tokenFlow.firstOrNull()
            if (token == null) {
                _isAnalyzing.value = false
                return@launch
            }

            try {
                // Kompres gambar dulu supaya ringan saat upload ke server
                val file = compressImage(context, photoUri)
                if (file == null) {
                    _isAnalyzing.value = false
                    return@launch
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Lempar ke server untuk dianalisa Gemini.
                repository.analyzeTask("Bearer $token", imagePart)
                delay(2000)
                refreshTasks()
                _isAnalyzing.value = false
            } catch (e: Exception) {
                Log.e("SmartSnap", "AI Error: ${e.message}")
                _isAnalyzing.value = false
            }
        }
    }

    // Fungsi hapus task dan refresh list tugas ketika berhasil
    fun deleteTask(taskId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.tokenFlow.firstOrNull() ?: return@launch
            try {
                repository.deleteTask("Bearer $token", taskId)
                fetchTasks()
                onSuccess()
            } catch (e: Exception) {
                Log.e("Detail", "Gagal menghapus: ${e.message}")
            }
        }
    }

    // Fungsi untuk update semua datatask sekaligus (menembak 6 endpoint berbeda).
    fun updateTaskData(
        taskId: Long, newTitle: String, newDescription: String, newPriority: String, newStatus: String, newDueDate: String, newImageUrl: String, onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _tasksState.value = UiState.Loading
            val token = tokenManager.tokenFlow.firstOrNull() ?: return@launch
            val bearerToken = "Bearer $token"

            try {
                repository.updateTitle(bearerToken, taskId, newTitle)
                repository.updateDescription(bearerToken, taskId, newDescription)
                repository.updatePriority(bearerToken, taskId, newPriority)
                repository.updateStatus(bearerToken, taskId, newStatus)
                repository.updateDueDate(bearerToken, taskId, newDueDate)
                repository.updateImageUrl(bearerToken, taskId, newImageUrl)

                // Fetch data agar layar Dashboard & Detail ter-update dengan informasi terbaru.
                fetchTasks()
                onSuccess()
            } catch (e: Exception) {
                Log.e("Detail", "Gagal update: ${e.message}")
                _tasksState.value = UiState.Error("Gagal perbarui data: ${e.message}")
            }
        }
    }
}

// Mengubah URI menjadi File fisik sesungguhnya agar bisa di-upload.
fun uriToFile(context: Context, uri: Uri): File? {
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }
    return null
}

// Mengecilkan resolusi & ukuran file gambar sebelum dikirim ke server.
fun compressImage(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) return null

        val maxResolution = 1080
        var width = bitmap.width
        var height = bitmap.height
        if (width > maxResolution || height > maxResolution) {
            val ratio = width.toFloat() / height.toFloat()
            if (ratio > 1) {
                width = maxResolution
                height = (maxResolution / ratio).toInt()
            } else {
                height = maxResolution
                width = (maxResolution * ratio).toInt()
            }
        }
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

class DashboardViewModelFactory(
    private val repository: AppRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == DashboardViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}