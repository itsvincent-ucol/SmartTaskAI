package com.example.SmartTaskAI.controller

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.service.TaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    // POST http://localhost:8080/api/tasks/analyze
    @PostMapping("/analyze")
    fun uploadAndAnalyze(
        @RequestParam("file") file: MultipartFile,
        // Menangkap input "title" dari Postman (opsional/boleh kosong)
        @RequestParam("title", required = false) rawTitle: String? 
    ): ResponseEntity<Task> {
        // Mengirim file dan judul ke Service
        val task = taskService.processAndSaveAITask(file, rawTitle)
        return ResponseEntity.ok(task)
    }

    // GET http://localhost:8080/api/tasks
    @GetMapping
    fun getAllTasks(): ResponseEntity<List<Task>> {
        val tasks = taskService.getAllTasks()
        return ResponseEntity.ok(tasks)
    }

    // PUT http://localhost:8080/api/tasks/{id}/priority
    @PutMapping("/{id}/priority")
    fun updatePriority(
        @PathVariable id: Long, 
        @RequestParam priority: String
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskPriority(id, priority)
        return ResponseEntity.ok(updatedTask)
    }

    // DELETE http://localhost:8080/api/tasks/{id}
    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        taskService.deleteTask(id)
        return ResponseEntity.ok(mapOf("message" to "Task berhasil dihapus"))
    }
}