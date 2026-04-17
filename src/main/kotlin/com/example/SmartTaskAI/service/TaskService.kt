package com.example.SmartTaskAI.service

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.net.InetAddress
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val aiService: AIService
) {

    // 1. Menentukan nama folder tempat menyimpan foto (Wajib ada di dalam class)
    private val UPLOAD_DIR = "uploads/"

    init {
        // Otomatis membuat folder "uploads" saat aplikasi pertama kali jalan
        val uploadDir = File(UPLOAD_DIR)
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }
    }

    fun processAndSaveAITask(file: MultipartFile, rawTitle: String?): Task {
        val finalTitle = rawTitle ?: "Laporan Tanpa Judul"
        
        // --- 2. PROSES SIMPAN FILE FISIK ---
        val originalFilename = file.originalFilename?.replace(" ", "_") ?: "image.jpg"
        val uniqueFilename = "${UUID.randomUUID()}_$originalFilename"
        val filePath = Paths.get(UPLOAD_DIR, uniqueFilename)
        
        Files.write(filePath, file.bytes) // Simpan gambar ke hard drive
        val savedImageUrl = "/uploads/$uniqueFilename" // Buat URL untuk Android
        // -----------------------------------

        val manualResult = runManualClassification(finalTitle)
        var finalPriority = manualResult.priority
        var finalDescription = manualResult.description

        val isOnline = isInternetAvailable()

        if (isOnline) {
            try {
                val aiAnalysis = aiService.analyzeImage(file)
                // Menggunakan savedImageUrl yang asli
                return saveTask(aiAnalysis.title, aiAnalysis.priority, aiAnalysis.description, savedImageUrl)
            } catch (e: Exception) {
                println("⚠️ AI Error (Mungkin Limit): ${e.message}")
            }
        } else {
            println("🌐 Server Offline: Fitur AI dinonaktifkan otomatis.")
            finalDescription += " (Mode Offline: AI dimatikan)"
        }

        // Menggunakan savedImageUrl yang asli
        return saveTask(finalTitle, finalPriority, finalDescription, savedImageUrl)
    }

    fun getAllTasks(): List<Task> {
        return taskRepository.findAll().sortedByDescending { it.createdAt }
    }

    fun deleteTask(id: Long) {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        
        // Hapus file fisik dari folder uploads
        if (task.imageUrl != null) {
            val fileName = task.imageUrl.substringAfterLast("/")
            val file = File(UPLOAD_DIR + fileName)
            if (file.exists()) {
                file.delete()
            }
        }
        
        taskRepository.deleteById(id)
    }

    fun updateTaskPriority(id: Long, newPriority: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        val updatedTask = task.copy(priority = newPriority)
        return taskRepository.save(updatedTask)
    }

    private fun isInternetAvailable(): Boolean {
        return try {
            val address = InetAddress.getByName("8.8.8.8")
            address.isReachable(2000)
        } catch (e: Exception) {
            false
        }
    }

    private fun runManualClassification(title: String): ManualCheck {
        val lower = title.lowercase()
        return when {
            lower.contains("mati") || lower.contains("server") || lower.contains("ujian") -> 
                ManualCheck("HIGH", "[Manual] Terdeteksi masalah kritis.")
            lower.contains("maintenance") || lower.contains("rutin") -> 
                ManualCheck("LOW", "[Manual] Laporan perawatan rutin.")
            else -> 
                ManualCheck("MEDIUM", "[Manual] Laporan ICT umum.")
        }
    }

    // Fungsi saveTask sekarang wajib menerima parameter imgUrl
    private fun saveTask(t: String, p: String, d: String, imgUrl: String): Task {
        return taskRepository.save(Task(
            title = t,
            priority = p,
            description = d,
            imageUrl = imgUrl,
            createdBy = "ICT Support Staff",
            createdAt = LocalDateTime.now()
        ))
    }

    data class ManualCheck(val priority: String, val description: String)
}