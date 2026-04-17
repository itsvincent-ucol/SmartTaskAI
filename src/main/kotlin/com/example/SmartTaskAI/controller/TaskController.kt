package com.example.SmartTaskAI.controller

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.service.TaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    @PostMapping("/analyze")
    fun uploadAndAnalyze(@RequestParam("file") file: MultipartFile): ResponseEntity<Task> {
        val savedTask = taskService.processAndSaveAITask(file)
        return ResponseEntity.ok(savedTask)
    }

    @GetMapping
    fun getAllTasks(): ResponseEntity<List<Task>> {
        return ResponseEntity.ok(taskService.getAllTasks())
    }
}