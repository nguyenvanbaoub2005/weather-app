package com.example.textn.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.textn.data.api.WeatherApiService
import com.example.textn.data.model.DayForecast
import com.example.textn.data.model.ForecastTabularData
import com.example.textn.data.model.HourlyData
import com.example.textn.data.model.HourlyForecast
import com.example.textn.data.network.RetrofitClient
import java.util.Date
import java.time.LocalDate
import java.time.LocalDate.of
import java.util.Calendar

class ForecastRepository(private val apiService: WeatherApiService) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getForecastData(
        lat: Double,
        lon: Double
    ): ForecastTabularData {
        val apiKey = "32ea3752b81cf12722a46358a7a9739c"

        try {
            // Call API to get forecast data using getWeatherForecast instead of getForecast
            val response = apiService.getWeatherForecast(lat, lon, apiKey)

            if (response.isSuccessful) {
                val weatherResponse = response.body()
                    ?: throw Exception("Empty response data")

                // Convert data from WeatherResponse to ForecastTabularData
                return ForecastTabularData(
                    latitude = weatherResponse.latitude,
                    longitude = weatherResponse.longitude,
                    modelName = "", // Empty as we don't need model info anymore
                    modelAccuracy = weatherResponse.modelAccuracy,
                    modelResolution = weatherResponse.modelResolution,
                    days = convertToForecastDays(weatherResponse.hourly),
                    updateTime = Date(System.currentTimeMillis())
                )
            } else {
                Log.e("API_ERROR", "Code: ${response.code()}, Message: ${response.message()}")
                throw Exception("Server error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Could not fetch forecast data. Error: ${e.message}")
        }
    }

    // Convert hourly data from WeatherResponse to list of DayForecast
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToForecastDays(hourlyData: List<HourlyData>): List<DayForecast> {
        val today = LocalDate.now()
        val allowedHours = listOf(1, 4, 7, 10, 13, 16, 19, 22) // Giờ tương ứng: 1AM, 4AM, ..., 10PM

        val groupedByDay = hourlyData.groupBy {
            Date(it.dt * 1000L).toLocalDate()
        }

        return groupedByDay
            .filterKeys { it.isAfter(today) }
            .toSortedMap() // Đảm bảo sắp xếp theo thứ tự ngày tăng dần
            .entries
            .take(5) // ✅ Lấy đúng 5 ngày tiếp theo
            .map { (date, hourlyEntries) ->
                DayForecast(
                    date = date.toDate(),
                    hourlyForecasts = hourlyEntries
                        .filter { entry ->
                            val calendar = Calendar.getInstance().apply { time = Date(entry.dt * 1000L) }
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                            hour in allowedHours
                        }
                        .map { hourly ->
                            HourlyForecast(
                                time = Date(hourly.dt * 1000L),
                                windSpeed = hourly.windSpeed,
                                windDirection = hourly.windDeg,
                                beaufortValue = calculateBeaufortScale(hourly.windSpeed),
                                beaufortGusts = calculateBeaufortScale(hourly.windGust ?: hourly.windSpeed),
                                temperature = hourly.temp,
                                feelsLike = hourly.feelsLike,
                                precipitation = (hourly.pop ?: 0.0).toFloat(),
                                waveHeight = null
                            )
                        }
                )
            }

    }




    // Calculate Beaufort scale from wind speed
    private fun calculateBeaufortScale(windSpeed: Float): Int {
        return when {
            windSpeed < 0.5 -> 0 // Calm
            windSpeed < 1.5 -> 1 // Light air
            windSpeed < 3.3 -> 2 // Light breeze
            windSpeed < 5.5 -> 3 // Gentle breeze
            windSpeed < 8.0 -> 4 // Moderate breeze
            windSpeed < 10.8 -> 5 // Fresh breeze
            windSpeed < 13.9 -> 6 // Strong breeze
            windSpeed < 17.2 -> 7 // High wind
            windSpeed < 20.7 -> 8 // Gale
            windSpeed < 24.4 -> 9 // Strong gale
            windSpeed < 28.4 -> 10 // Storm
            windSpeed < 32.6 -> 11 // Violent storm
            else -> 12 // Hurricane
        }
    }

    // Extension utility to convert between Date and LocalDate
    @RequiresApi(Build.VERSION_CODES.O)
    private fun Date.toLocalDate(): LocalDate {
        val calendar = Calendar.getInstance().apply { time = this@toLocalDate }
        return of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LocalDate.toDate(): Date {
        val calendar = Calendar.getInstance().apply {
            set(year, monthValue - 1, dayOfMonth)
        }
        return calendar.time
    }

    companion object {
        // Factory method to create repository
        fun create(): ForecastRepository {
            val apiService = RetrofitClient.instance
            return ForecastRepository(apiService)
        }
    }
}