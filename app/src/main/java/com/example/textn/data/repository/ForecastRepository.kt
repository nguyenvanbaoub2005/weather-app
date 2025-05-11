package com.example.textn.data.repository

import android.util.Log
import com.example.textn.data.api.WeatherApiService
import com.example.textn.data.model.DayForecast
import com.example.textn.data.model.ForecastTabularData
import com.example.textn.data.model.HourlyForecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class ForecastRepository(private val apiService: WeatherApiService) {

    suspend fun getForecastData(latitude: Double, longitude: Double, model: String): ForecastTabularData {
        return withContext(Dispatchers.IO) {
            try {
                // We'd normally call the API here, but for demo purposes we'll create mock data
                // that resembles what's shown in the Windy app screenshot
                createMockForecastData(latitude, longitude, model)
            } catch (e: Exception) {
                Log.e("ForecastRepository", "Error fetching forecast data", e)
                throw e
            }
        }
    }

    private fun createMockForecastData(latitude: Double, longitude: Double, model: String): ForecastTabularData {
        val calendar = Calendar.getInstance()
        val startTime = calendar.time

        // Create 5 days of forecast
        val days = mutableListOf<DayForecast>()

        for (dayOffset in 0..4) {
            calendar.time = startTime
            calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
            val dayDate = calendar.time

            val hourlyForecasts = mutableListOf<HourlyForecast>()

            // 8 time slots per day (3 hour intervals)
            for (hourSlot in 0..7) {
                calendar.time = dayDate
                calendar.set(Calendar.HOUR_OF_DAY, 7 + (hourSlot * 3) % 24)

                // Create realistic-looking wind and weather patterns
                val windBase = 3.5f + (Math.sin(dayOffset * 0.5 + hourSlot * 0.3) * 1.5).toFloat()
                val windDirection = ((hourSlot * 45 + dayOffset * 20) % 360)
                val beaufort = calculateBeaufort(windBase)
                val beaufortGusts = beaufort + if (hourSlot % 3 == 0) 1 else 0

                val tempBase = 27f + (Math.sin(dayOffset * 0.8 + hourSlot * 0.4) * 5).toFloat()

                hourlyForecasts.add(
                    HourlyForecast(
                        time = calendar.time,
                        windSpeed = windBase,
                        windDirection = windDirection,
                        beaufortValue = beaufort,
                        beaufortGusts = beaufortGusts,
                        temperature = tempBase,
                        feelsLike = tempBase + 1f,
                        precipitation = if (beaufort > 3) 0.2f else null,
                        waveHeight = if (model == "GFS27") 0.4f else null
                    )
                )
            }

            days.add(DayForecast(dayDate, hourlyForecasts))
        }

        return ForecastTabularData(
            latitude = latitude,
            longitude = longitude,
            modelName = model,
            modelAccuracy = 76,
            modelResolution = 27,
            days = days,
            updateTime = Date()
        )
    }

    private fun calculateBeaufort(windSpeed: Float): Int {
        return when {
            windSpeed < 0.5f -> 0
            windSpeed < 1.5f -> 1
            windSpeed < 3.3f -> 2
            windSpeed < 5.5f -> 3
            windSpeed < 7.9f -> 4
            windSpeed < 10.7f -> 5
            windSpeed < 13.8f -> 6
            windSpeed < 17.1f -> 7
            windSpeed < 20.7f -> 8
            windSpeed < 24.4f -> 9
            windSpeed < 28.4f -> 10
            windSpeed < 32.6f -> 11
            else -> 12
        }
    }
}