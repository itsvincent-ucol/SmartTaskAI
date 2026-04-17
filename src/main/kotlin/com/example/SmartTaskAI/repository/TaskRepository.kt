package com.example.SmartTaskAI.repository

import com.example.SmartTaskAI.model.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long>