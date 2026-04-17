package com.example.SmartTaskAI.controller

import com.example.SmartTaskAI.dto.AuthResponse
import com.example.SmartTaskAI.dto.LoginRequest
import com.example.SmartTaskAI.dto.RegisterRequest
import com.example.SmartTaskAI.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    // POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val response: AuthResponse = authService.register(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            // Mengembalikan error (misal: "Email sudah terdaftar!")
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            val response: AuthResponse = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            // Mengembalikan error 401 Unauthorized jika password salah
            ResponseEntity.status(401).body(mapOf("error" to e.message))
        }
    }
}