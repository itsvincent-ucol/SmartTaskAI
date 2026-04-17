package com.example.SmartTaskAI.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil {

    // Kunci Rahasia. Di aplikasi asli, ini harus super panjang dan disimpan di application.properties
    private val SECRET_KEY = "IniAdalahKunciRahasiaSmartTaskAplikasiICTSupportSangatPanjangSekali"
    
    // Masa berlaku Token (misal: 24 jam)
    private val EXPIRATION_TIME: Long = 1000 * 60 * 60 * 24

    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(SECRET_KEY.toByteArray())
    }

    // Fungsi untuk membuat Token baru saat Login sukses
    fun generateToken(email: String, role: String): String {
        return Jwts.builder()
            .subject(email)
            .claim("role", role) // Menyisipkan data peran (misal: ROLE_ICT) ke dalam token
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey())
            .compact()
    }

    // Fungsi untuk membaca Email (Subject) dari Token
    fun extractEmail(token: String): String {
        val claims: Claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
        return claims.subject
    }

    // Fungsi untuk mengecek apakah Token masih berlaku (belum expired)
    fun validateToken(token: String, emailFromDb: String): Boolean {
        val extractedEmail = extractEmail(token)
        return (extractedEmail == emailFromDb && !isTokenExpired(token))
    }

    private fun isTokenExpired(token: String): Boolean {
        val claims: Claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
        return claims.expiration.before(Date())
    }
}