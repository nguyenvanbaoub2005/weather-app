package com.example.textn.data.model

data class GeminiRequestBody(
    val contents: List<Content>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

data class Content(
    val parts: List<Part>,
    val role: String = "user"
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 1024
)

