package com.example.SmartTaskAI.repository

import com.example.SmartTaskAI.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    // Function yang tersedia di Spring Data JPA yang secara otomatis membuat query "SELECT * FROM users WHERE email = ?"
    fun findByEmail(email: String): User?
}