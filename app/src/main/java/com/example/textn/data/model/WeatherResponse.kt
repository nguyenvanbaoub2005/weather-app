package com.example.textn.data.model

data class WeatherResponse(
    val current: CurrentWeather,
    val daily: List<DailyWeather>
)

data class CurrentWeather(
    val temp: Double,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<WeatherDescription>
)

data class DailyWeather(
    val dt: Long,
    val temp: Temperature,
    val humidity: Int,             // ✅ Bổ sung để hiển thị % độ ẩm
    val wind_speed: Double,        // ✅ Bổ sung để hiển thị tốc độ gió
    val weather: List<WeatherDescription>
)

data class Temperature(
    val day: Double
)

data class WeatherDescription(
    val description: String,
    val icon: String               // ✅ Bổ sung để lấy icon thời tiết
)
