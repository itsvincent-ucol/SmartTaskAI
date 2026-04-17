package com.example.SmartTaskAI.service

import com.example.SmartTaskAI.dto.AuthResponse
import com.example.SmartTaskAI.dto.LoginRequest
import com.example.SmartTaskAI.dto.RegisterRequest
import com.example.SmartTaskAI.model.User
import com.example.SmartTaskAI.repository.UserRepository
import com.example.SmartTaskAI.security.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    fun register(request: RegisterRequest): AuthResponse {
        // Memaksa data dari DTO menjadi String mutlak
        val reqEmail: String = request.email?.toString() ?: ""
        val reqPass: String = request.password?.toString() ?: ""

        if (userRepository.findByEmail(reqEmail) != null) {
            throw RuntimeException("Email sudah terdaftar!")
        }

        // Memaksa hasil enkripsi Java menjadi String mutlak Kotlin
        val hashed: String = passwordEncoder.encode(reqPass) ?: ""
        
        val newUser = User(
            email = reqEmail,
            password = hashed,
            role = "ROLE_ICT"
        )
        userRepository.save(newUser)

        // Memastikan parameter untuk Token adalah String
        val tokenStr: String = jwtUtil.generateToken(newUser.email, newUser.role)
        return AuthResponse(tokenStr, newUser.email, newUser.role)
    }

    fun login(request: LoginRequest): AuthResponse {
        val reqEmail: String = request.email?.toString() ?: ""
        val reqPass: String = request.password?.toString() ?: ""

        val user = userRepository.findByEmail(reqEmail) 
            ?: throw RuntimeException("Email atau Password salah!")

        // Memaksa password dari database menjadi String mutlak
        val dbPass: String = user.password?.toString() ?: ""

        if (!passwordEncoder.matches(reqPass, dbPass)) {
            throw RuntimeException("Email atau Password salah!")
        }

        val tokenStr: String = jwtUtil.generateToken(user.email, user.role)
        return AuthResponse(tokenStr, user.email, user.role)
    }
}