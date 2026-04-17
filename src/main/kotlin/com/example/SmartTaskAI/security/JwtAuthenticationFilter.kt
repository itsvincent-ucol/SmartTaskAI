package com.example.SmartTaskAI.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. Ambil header Authorization dari Postman
        val authHeader = request.getHeader("Authorization")

        // 2. Cek apakah formatnya benar (dimulai dengan "Bearer ")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7) // Potong kata "Bearer " untuk mengambil token aslinya
            
            try {
                val email = jwtUtil.extractEmail(token)

                // 3. Jika token valid dan user belum terotentikasi di memori Spring
                if (email != null && SecurityContextHolder.getContext().authentication == null) {
                    // Beri izin masuk dengan memberikan "Pass" resmi dari Spring Security
                    val authToken = UsernamePasswordAuthenticationToken(email, null, emptyList())
                    SecurityContextHolder.getContext().authentication = authToken
                }
            } catch (e: Exception) {
                println("Token JWT ditolak: ${e.message}")
            }
        }
        
        // 4. Lanjutkan perjalanan request ke Controller (atau blokir jika authentication masih null)
        filterChain.doFilter(request, response)
    }
}