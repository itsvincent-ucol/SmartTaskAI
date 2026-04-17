package com.example.SmartTaskAI.controller

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.service.TaskService
import com.example.SmartTaskAI.dto.TaskRequest // IMPORT YANG HILANG SEBELUMNYA
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    @PostMapping("/analyze")
    fun uploadAndAnalyze(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("title", required = false) rawTitle: String?
    ): ResponseEntity<Task> {
        val task = taskService.processAndSaveAITask(file, rawTitle)
        return ResponseEntity.ok(task)
    }

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

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        taskService.deleteTask(id)
        return ResponseEntity.ok(mapOf("message" to "Task berhasil dihapus"))
    }
}