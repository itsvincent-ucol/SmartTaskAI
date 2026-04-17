package com.example.SmartTaskAI.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // Jika tidak ada token atau formatnya salah, biarkan lewat (nanti akan diblokir oleh SecurityConfig)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        // Ambil tokennya saja dengan membuang awalan "Bearer "
        val token = authHeader.substring(7)

        try {
            val userEmail = jwtUtil.extractEmail(token)

            // Jika email ada di token dan user belum login di konteks saat ini
            if (userEmail != null && SecurityContextHolder.getContext().authentication == null) {
                
                if (jwtUtil.validateToken(token, userEmail)) {
                    // Ekstrak peran (role) dari dalam token
                    val claims = io.jsonwebtoken.Jwts.parser()
                        .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("IniAdalahKunciRahasiaSmartTaskAplikasiICTSupportSangatPanjangSekali".toByteArray()))
                        .build()
                        .parseSignedClaims(token)
                        .payload
                        
                    val role = claims.get("role", String::class.java) ?: "ROLE_ICT"

                    // Beri izin masuk ke dalam sistem Spring Security
                    val authToken = UsernamePasswordAuthenticationToken(
                        userEmail, 
                        null, 
                        listOf(SimpleGrantedAuthority(role))
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (e: Exception) {
            // Token tidak valid, kedaluwarsa, atau rusak
            println("Error Validasi JWT: ${e.message}")
        }

        // Lanjutkan request ke tahap berikutnya
        filterChain.doFilter(request, response)
    }
}