package com.example.SmartTaskAI.controller

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.service.TaskService
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

    // ==========================================
    // AREA UPDATE DATA (SINGLE-FIELD UPDATE)
    // ==========================================

    @PutMapping("/{id}/priority")
    fun updatePriority(
        @PathVariable id: Long,
        @RequestParam priority: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskPriority(id, priority)
        return ResponseEntity.ok(updatedTask)
    }

    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestParam status: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskStatus(id, status)
        return ResponseEntity.ok(updatedTask)
    }

    @PutMapping("/{id}/title")
    fun updateTitle(
        @PathVariable id: Long,
        @RequestParam title: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskTitle(id, title)
        return ResponseEntity.ok(updatedTask)
    }

    @PutMapping("/{id}/description")
    fun updateDescription(
        @PathVariable id: Long,
        @RequestParam description: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskDescription(id, description)
        return ResponseEntity.ok(updatedTask)
    }

    @PutMapping("/{id}/due_date")
    fun updateDueDate(
        @PathVariable id: Long,
        @RequestParam("due_date") dueDate: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskDueDate(id, dueDate)
        return ResponseEntity.ok(updatedTask)
    }

    @PutMapping("/{id}/image_url")
    fun updateImageUrl(
        @PathVariable id: Long,
        @RequestParam("image_url") imageUrl: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskImageUrl(id, imageUrl)
        return ResponseEntity.ok(updatedTask)
    }

    // ==========================================
    // AREA DELETE DATA
    // ==========================================

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        taskService.deleteTask(id)
        return ResponseEntity.ok(mapOf("message" to "Task berhasil dihapus"))
    }
}