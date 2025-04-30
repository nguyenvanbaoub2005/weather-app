package com.example.textn.data.model

// Class đại diện cho dữ liệu thời tiết
data class WeatherData(
    val temperature: Float,
    val humidity: Float,
    val condition: String,
    val airQuality: Int? = null,
    val uvIndex: Float? = null,
    val location: String
)