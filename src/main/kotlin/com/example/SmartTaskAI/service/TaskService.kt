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

    init {
        val uploadDir = File(UPLOAD_DIR)
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }
    }

    // --- HELPER FUNCTION: Biar tidak copy-paste logika simpan file ---
    private fun uploadFile(file: MultipartFile): String {
        val originalFilename = file.originalFilename?.replace(" ", "_") ?: "image.jpg"
        val uniqueFilename = "${UUID.randomUUID()}_$originalFilename"
        val filePath = Paths.get(UPLOAD_DIR, uniqueFilename)
        Files.write(filePath, file.bytes)
        return "/uploads/$uniqueFilename"
    }

    fun processAndSaveAITask(file: MultipartFile, rawTitle: String?): Task {
        val finalTitle = rawTitle ?: "Laporan Tanpa Judul"
        
        // Gunakan helper uploadFile
        val savedImageUrl = uploadFile(file)

        val manualResult = runManualClassification(finalTitle)
        var finalPriority = manualResult.priority
        var finalDescription = manualResult.description

        val isOnline = isInternetAvailable()

        if (isOnline) {
            try {
                val aiAnalysis = aiService.analyzeImage(file)
                return saveTask(aiAnalysis.title, aiAnalysis.priority, aiAnalysis.description, savedImageUrl)
            } catch (e: Exception) {
                println("AI Error (Mungkin kena limit): ${e.message}")
            }
        } else {
            finalDescription += " (Offline AI dimatikan)"
        }

        return saveTask(finalTitle, finalPriority, finalDescription, savedImageUrl)
    }

    fun getAllTasks(): List<Task> {
        return taskRepository.findAll().sortedByDescending { it.createdAt }
    }

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

    fun updateTaskPriority(id: Long, newPriority: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        return taskRepository.save(task.copy(priority = newPriority))
    }

    fun updateTaskStatus(id: Long, newStatus: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        return taskRepository.save(task.copy(status = newStatus))
    }

    // ========================================================
    // FITUR UPDATE BARU (UNTUK SINGLE-FIELD UPDATE)
    // ========================================================

    fun updateTaskTitle(id: Long, newTitle: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        return taskRepository.save(task.copy(title = newTitle))
    }

    fun updateTaskDescription(id: Long, newDescription: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        return taskRepository.save(task.copy(description = newDescription))
    }

    fun updateTaskDueDate(id: Long, newDueDate: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        
        // Ubah format String (cth: "2026-04-30") menjadi LocalDate agar bisa disimpan
        val parsedDate = try {
            LocalDate.parse(newDueDate)
        } catch (e: Exception) {
            LocalDate.now() // Jika format salah, kembalikan ke tanggal hari ini
        }
        
        return taskRepository.save(task.copy(dueDate = parsedDate))
    }

    fun updateTaskImageUrl(id: Long, newImageUrl: String): Task {
        val task = taskRepository.findById(id).orElseThrow { RuntimeException("Task dengan ID $id tidak ditemukan") }
        return taskRepository.save(task.copy(imageUrl = newImageUrl))
    }

    // ========================================================

    // --- PERBAIKAN: Tambahkan parameter 'file' di sini ---
    fun createManualTask(
        title: String, 
        description: String, 
        priority: String, 
        dueDate: LocalDate?,
        file: MultipartFile? // Parameter baru agar tidak error saat dipanggil Controller
    ): Task {
        
        // Logika simpan foto jika user melampirkan foto di mode manual
        val savedImageUrl = if (file != null && !file.isEmpty) {
            uploadFile(file)
        } else {
            null
        }

        val task = Task(
            title = title,
            description = description,
            priority = priority,
            status = "PENDING",
            imageUrl = savedImageUrl, // Sekarang bisa simpan foto walau manual
            dueDate = dueDate ?: LocalDate.now(),
            createdBy = "ICT Support Staff",
            createdAt = LocalDateTime.now()
        )
        return taskRepository.save(task)
    }

    private fun isInternetAvailable(): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 3000)
                true
            }
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
                // PERBAIKAN: Diubah menjadi "MID" agar terbaca oleh tombol UI Android
                ManualCheck("MEDIUM", "[Manual] Laporan ICT umum.")
        }
    }

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