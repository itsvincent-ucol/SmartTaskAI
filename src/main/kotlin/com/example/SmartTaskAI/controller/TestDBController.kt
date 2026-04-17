package com.example.SmartTaskAI.controller

import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
// API untuk melakukan pengecheckkan koneksi dari backend ke server database secara langsung
@RequestMapping("/api/test")
class TestDbController(private val jdbcTemplate: JdbcTemplate) {

    @GetMapping("/db")
    fun testConnection(): ResponseEntity<String> {
        return try {
            // Mengeksekusi query paling dasar ke MySQL
            jdbcTemplate.execute("SELECT 1")
            ResponseEntity.ok("✅ BERHASIL! Spring Boot sukses terkoneksi ke MySQL Cloud.")
        } catch (e: Exception) {
            // Menangkap error jika koneksi masih ditolak atau terputus
            ResponseEntity.internalServerError().body("❌ GAGAL terhubung: ${e.message}")
        }
    }
}