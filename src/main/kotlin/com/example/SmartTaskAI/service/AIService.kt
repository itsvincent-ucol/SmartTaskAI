package com.example.SmartTaskAI.service

import com.example.SmartTaskAI.dto.TaskAIResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.util.Base64

@Service
class AIService {

    // Tersimpan di application.properties
    @Value("\${gemini.api.key}")
    private lateinit var apiKey: String

    private val objectMapper = ObjectMapper()
    private val restTemplate = RestTemplate()

    fun analyzeImage(file: MultipartFile): TaskAIResponse {
        val mimeType = file.contentType ?: "image/jpeg"
        val base64Image = Base64.getEncoder().encodeToString(file.bytes)
        
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        
        val promptText = """
            Anda adalah asisten AI untuk ICT Support Sekolah. 
            Analisis gambar masalah IT ini. 
            Berikan respons HANYA dalam format JSON dengan struktur: 
            {"title": "Judul Singkat Masalah", "priority": "HIGH/MEDIUM/LOW", "description": "Penjelasan singkat"}.
            Aturan Prioritas:
            - HIGH: Jika menyangkut server, switch inti, akses internet massal, atau alat ujian.
            - MEDIUM: Masalah pada satu komputer/printer.
            - LOW: Masalah estetika kabel atau minor.
        """.trimIndent()

        val payload = mapOf(
            "contents" to listOf(
                mapOf("parts" to listOf(
                    mapOf("text" to promptText),
                    mapOf("inline_data" to mapOf("mime_type" to mimeType, "data" to base64Image))
                ))
            )
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity(payload, headers)

        try {
            val response = restTemplate.postForObject(url, request, String::class.java)
            val jsonClean = extractJsonFromResponse(response ?: "")
            return objectMapper.readValue(jsonClean, TaskAIResponse::class.java)
        } catch (e: Exception) {
            println("ERROR DARI GEMINI: ${e.message}")
            // Mengembalikan nilai dummy JIKA terkena limit (agar aplikasi tidak crash)
            return TaskAIResponse(
                title = "Error Limit/Koneksi AI",
                priority = "MEDIUM",
                description = "AI gagal memproses gambar karena limit kuota. Pesan error: ${e.message}"
            )
        }
    }

    private fun extractJsonFromResponse(response: String): String {
        val rootNode = objectMapper.readTree(response)
        val textNode = rootNode.path("candidates").get(0)
            .path("content").path("parts").get(0)
            .path("text").asText()
            
        return textNode.replace("```json", "").replace("```", "").trim()
    }
}
