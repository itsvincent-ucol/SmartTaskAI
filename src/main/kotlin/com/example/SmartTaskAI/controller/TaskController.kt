package com.example.SmartTaskAI.controller

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.service.TaskService
import com.example.SmartTaskAI.dto.TaskRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
// Buat API untuk membuat task baru
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    // Jika ada internet di device pengguna maka akan menggunakan Gemini API AI untuk menganalisa
    @PostMapping("/analyze")
    fun uploadAndAnalyze(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("title", required = false) rawTitle: String?
    ): ResponseEntity<Task> {
        val task = taskService.processAndSaveAITask(file, rawTitle)
        return ResponseEntity.ok(task)
    }

    // Jika user ingin memasukkan secara langsung dan sudah mengetahui masalahnya
    @PostMapping("/manual")
    fun createManualTask(@RequestBody request: TaskRequest): ResponseEntity<Task> {
        val task = taskService.createManualTask(
            title = request.title,
            description = request.description,
            priority = request.priority,
            dueDate = request.dueDate
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