package com.example.SmartTaskAI.controller

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.service.TaskService
// TaskRequest tidak lagi dipakai di endpoint ini karena kita menerima Multipart
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = ["*"]) // Menghindari masalah pemblokiran CORS dari Android
class TaskController(private val taskService: TaskService) {

    // Jika ada internet di device pengguna maka akan menggunakan Gemini API AI untuk menganalisa
    @PostMapping(value = ["/analyze"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAndAnalyze(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("title", required = false) rawTitle: String?
    ): ResponseEntity<Task> {
        val task = taskService.processAndSaveAITask(file, rawTitle)
        return ResponseEntity.ok(task)
    }

    // PERBAIKAN: Menggunakan @RequestParam agar bisa membaca Teks + Foto dari Android
    @PostMapping(value = ["/manual"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createManualTask(
        @RequestParam("title") title: String,
        @RequestParam("description") description: String,
        @RequestParam("priority") priority: String,
        @RequestParam(value = "file", required = false) file: MultipartFile? // Foto opsional
    ): ResponseEntity<Task> {
        
        val task = taskService.createManualTask(
            title = title,
            description = description,
            priority = priority,
            dueDate = null, // Atur default null, atau sesuaikan jika butuh input tanggal
            file = file     // Kirim file ini ke Service untuk disimpan
        )
        return ResponseEntity.ok(task)
    }

    @GetMapping
    fun getAllTasks(): ResponseEntity<List<Task>> {
        val tasks = taskService.getAllTasks()
        return ResponseEntity.ok(tasks)
    }

    // API untuk mengatur priority dari task
    @PutMapping("/{id}/priority")
    fun updatePriority(
        @PathVariable id: Long,
        @RequestParam priority: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskPriority(id, priority)
        return ResponseEntity.ok(updatedTask)
    }

    // API untuk mengatur status dari task
    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestParam status: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskStatus(id, status)
        return ResponseEntity.ok(updatedTask)
    }

    // API untuk menghapus task yang ada
    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        taskService.deleteTask(id)
        return ResponseEntity.ok(mapOf("message" to "Task berhasil dihapus"))
    }
}