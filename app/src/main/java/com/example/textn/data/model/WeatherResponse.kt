package com.example.textn.data.model

data class WeatherResponse(
    val current: CurrentWeather,
    val daily: List<DailyWeather>
)

data class CurrentWeather(
    val temp: Double,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<WeatherDescription>,
    val airQuality: AirQuality? = null,
    val uvIndex: Double? = null, // Chỉ số tia UV cho thời gian hiện tại
    val uvi: Double? = null      // Thêm trường uvi (UV Index)
)

data class DailyWeather(
    val dt: Long,
    val temp: Temperature,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<WeatherDescription>,
    val airQuality: AirQuality? = null,
    val uvi: Double? = null     // Thêm trường uvi (UV Index) cho dự báo hàng ngày
)

data class Temperature(
    val day: Double,
    val night: Double? = null,
    val feels_like: Double? = null
)

data class WeatherDescription(
    val description: String,
    val icon: String,
    val main: String? = null
)

data class AirQuality(
    val aqi: Int?  // Chỉ số chất lượng không khí
)
