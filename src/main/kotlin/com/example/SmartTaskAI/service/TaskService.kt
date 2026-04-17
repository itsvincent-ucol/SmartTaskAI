package com.example.SmartTaskAI.service

import com.example.SmartTaskAI.model.Task
import com.example.SmartTaskAI.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val aiService: AIService
) {
    fun processAndSaveAITask(file: MultipartFile): Task {
        // 1. Lempar gambar ke AI
        val aiAnalysis = aiService.analyzeImage(file)
        
        // 2. Buat objek Task dari hasil AI
        val newTask = Task(
            title = aiAnalysis.title,
            description = aiAnalysis.description,
            priority = aiAnalysis.priority,
            // Nanti imageUrl bisa diisi URL setelah Anda mengintegrasikan Cloud Storage/MinIO
            imageUrl = "image_uploaded_${System.currentTimeMillis()}.jpg" 
        )
        
        // 3. Simpan ke Database
        return taskRepository.save(newTask)
    }

    fun getAllTasks(): List<Task> = taskRepository.findAll()
}