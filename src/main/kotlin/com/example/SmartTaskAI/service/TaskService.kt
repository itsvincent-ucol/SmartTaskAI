package com.example.SmartTaskAI.service

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.LocalDate
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val aiService: AIService
) {

    private val UPLOAD_DIR = "uploads/"

    // Dijalankan pertama kali saat aplikasi start
    init {
        val uploadDir = File(UPLOAD_DIR) // Letak direktori file yang diupload
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }
    }

    fun processAndSaveAITask(file: MultipartFile, rawTitle: String?): Task {
        val finalTitle = rawTitle ?: "Laporan Tanpa Judul"
        
        // Mengubah nama file menjadi unik (pakai UUID) agar tidak ada yang sama nama filenya
        val originalFilename = file.originalFilename?.replace(" ", "_") ?: "image.jpg"
        val uniqueFilename = "${UUID.randomUUID()}_$originalFilename"
        val filePath = Paths.get(UPLOAD_DIR, uniqueFilename)

        // Menulis byte file ke dalam folder uploads
        Files.write(filePath, file.bytes)
        val savedImageUrl = "/uploads/$uniqueFilename"

        val manualResult = runManualClassification(finalTitle)
        var finalPriority = manualResult.priority
        var finalDescription = manualResult.description

        // Cek apakah ada internet untuk memanggil Gemini AI
        val isOnline = isInternetAvailable()

        if (isOnline) {
            try {
                // AI Service untuk menganalisa gambar dengan API AI
                val aiAnalysis = aiService.analyzeImage(file)

                // Jika AI berhasil, langsung simpan dan return hasilnya
                return saveTask(aiAnalysis.title, aiAnalysis.priority, aiAnalysis.description, savedImageUrl)
            } catch (e: Exception) {
                // Jika AI gagal masukkan log error dan lanjut ke mode offline
                println("AI Error (Mungkin kena limit): ${e.message}")
            }
        } else {
            finalDescription += " (Offline  AI dimatikan)"
        }

        return saveTask(finalTitle, finalPriority, finalDescription, savedImageUrl)
    }

    // Mengambil semua task yang ada dan diurutkan dari yang terbaru
    fun getAllTasks(): List<Task> {
        return taskRepository.findAll().sortedByDescending { it.createdAt }
    }

    // Menghapus task beserta foto yang diupload user pada folder uploads
    fun deleteTask(id: Long) {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        if (task.imageUrl != null) {
            val fileName = task.imageUrl.substringAfterLast("/")
            val file = File(UPLOAD_DIR + fileName)
            if (file.exists()) {
                file.delete()
            }
        }
        taskRepository.deleteById(id)
    }

    // Update prioritas
    fun updateTaskPriority(id: Long, newPriority: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        return taskRepository.save(task.copy(priority = newPriority))
    }

    // Update status task
    fun updateTaskStatus(id: Long, newStatus: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        return taskRepository.save(task.copy(status = newStatus))
    }

    // Membuat task secara manual (tanpa upload file dan AI)
    fun createManualTask(title: String, description: String, priority: String, dueDate: LocalDate?): Task {
        val task = Task(
            title = title,
            description = description,
            priority = priority,
            status = "PENDING",
            imageUrl = null, // Laporan manual tidak punya foto
            dueDate = dueDate ?: LocalDate.now(), // Secara default menggunakan tanggal saat itu juga task dibuat
            createdBy = "ICT Support Staff",
            createdAt = LocalDateTime.now()
        )
        return taskRepository.save(task)
    }

    // Function untuk cek koneksi Internet dengan metode TCP Socket (Aman untuk Linux/VPS)
    private fun isInternetAvailable(): Boolean {
        return try {
            Socket().use { socket ->
                // Ketuk pintu server DNS Google (8.8.8.8) di port 53 (DNS) dengan batas waktu 3 detik
                socket.connect(InetSocketAddress("8.8.8.8", 53), 3000)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    // Logika klasifikasi sederhana jika tidak ada internet
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

    // Helper function untuk menyimpan objek Task ke database
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