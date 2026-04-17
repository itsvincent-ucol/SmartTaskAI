package com.example.SmartTaskAI.config

import com.example.SmartTaskAI.security.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) { // Menyuntikkan filter JWT di sini

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Endpoint Publik (Bisa diakses siapa saja, TERMASUK folder uploads)
                    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/test/db", "/uploads/**").permitAll()
                    // Semua rute lainnya wajib menggunakan Token
                    .anyRequest().authenticated()
            }
            // Tambahkan filter JWT kita SEBELUM filter autentikasi standar milik Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        
        return http.build()
    }
}