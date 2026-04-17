package com.example.SmartTaskAI.repository

import com.example.SmartTaskAI.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    // Fungsi ajaib Spring Data JPA: Otomatis membuat query "SELECT * FROM users WHERE email = ?"
    fun findByEmail(email: String): User?
}