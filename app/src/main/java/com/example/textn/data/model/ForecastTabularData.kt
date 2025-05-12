package com.example.textn.data.model

import java.util.Date

data class ForecastTabularData(
    val latitude: Double,
    val longitude: Double,
    val modelName: String,
    val modelAccuracy: Int,
    val modelResolution: Int,
    val days: List<DayForecast>,
    val updateTime: Date
)

data class DayForecast(
    val date: Date,
    val hourlyForecasts: List<HourlyForecast>
)

data class HourlyForecast(
    val time: Date,
    val windSpeed: Float,  // in m/s
    val windDirection: Int, // in degrees
    val beaufortValue: Int, // Beaufort scale (0-12)
    val beaufortGusts: Int, // Beaufort scale for gusts
    val temperature: Float, // in °C
    val feelsLike: Float,  // in °C
    val precipitation: Float?, // in mm
    val waveHeight: Float?  // in meters
)
