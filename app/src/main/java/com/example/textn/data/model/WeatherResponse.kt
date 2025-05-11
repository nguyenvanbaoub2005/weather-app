package com.example.textn.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeather,
    val daily: List<DailyWeather>,
    //
    val latitude: Double,
    val longitude: Double,
    val model: String,
    @SerializedName("model_resolution")
    val modelResolution: Int,
    @SerializedName("model_accuracy")
    val modelAccuracy: Int,
    val forecasts: List<ForecastItem>,
    @SerializedName("update_time")
    val updateTime: String
)

data class ForecastItem(
    val time: String,
    @SerializedName("wind_speed")
    val windSpeed: Float,
    @SerializedName("wind_direction")
    val windDirection: Int,
    @SerializedName("temperature")
    val temperature: Float,
    @SerializedName("feels_like")
    val feelsLike: Float,
    val precipitation: Float?,
    @SerializedName("wave_height")
    val waveHeight: Float?
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
