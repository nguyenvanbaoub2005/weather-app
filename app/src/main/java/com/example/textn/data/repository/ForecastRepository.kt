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
import java.time.ZoneId
import java.util.Calendar

class ForecastRepository(private val apiService: WeatherApiService) {

    // Di chuyển API key vào hằng số riêng để dễ bảo trì
    companion object {
        private const val API_KEY = "32ea3752b81cf12722a46358a7a9739c"

        // Factory method to create repository
        fun create(): ForecastRepository {
            val apiService = RetrofitClient.instance
            return ForecastRepository(apiService)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getForecastData(
        lat: Double,
        lon: Double
    ): ForecastTabularData {
        try {
            // Gọi API để lấy dữ liệu dự báo
            val response = apiService.getWeatherForecast(lat, lon, API_KEY)

            if (response.isSuccessful) {
                val weatherResponse = response.body()
                    ?: throw Exception("Empty response data")

                // Chuyển đổi từ WeatherResponse sang ForecastTabularData
                return ForecastTabularData(
                    latitude = weatherResponse.latitude,
                    longitude = weatherResponse.longitude,
                    modelName = "OpenWeatherMap", // Sử dụng tên nguồn dữ liệu
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

    // Hàm chuyển đổi dữ liệu theo giờ từ WeatherResponse thành danh sách DayForecast
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToForecastDays(hourlyData: List<HourlyData>): List<DayForecast> {
        val today = LocalDate.now()
        val allowedHours = listOf(1, 4, 7, 10, 13, 16, 19, 22)

        // Nhóm dữ liệu theo ngày
        val groupedByDay = hourlyData.groupBy {
            Date(it.dt * 1000L).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }

        return groupedByDay
            .filterKeys { it.isAfter(today) }
            .toSortedMap()
            .entries
            .take(2) // lấy 2 ngày
            .map { (date, hourlyEntries) ->
                val forecastsByHour = hourlyEntries.associateBy { entry ->
                    Calendar.getInstance().apply { time = Date(entry.dt * 1000L) }
                        .get(Calendar.HOUR_OF_DAY)
                }

                val hourlyForecasts = allowedHours.mapNotNull { hour ->
                    val matched = forecastsByHour[hour]
                        ?: forecastsByHour.entries.minByOrNull { entry ->
                            kotlin.math.abs(entry.key - hour)
                        }?.value

                    matched?.let { hourly ->
                        HourlyForecast(
                            time = Date(hourly.dt * 1000L),
                            windSpeed = hourly.windSpeed,
                            windDirection = hourly.windDeg,
                            beaufortValue = calculateBeaufortScale(hourly.windSpeed),
                            beaufortGusts = calculateBeaufortScale(hourly.windGust ?: hourly.windSpeed),
                            temperature = hourly.temp,
                            feelsLike = hourly.feelsLike,
                            precipitation = hourly.pop?.let { it.toFloat() * 10f } ?: 0f,
                            waveHeight = null
                        )
                    }
                }

                DayForecast(
                    date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().let { Date.from(it) },
                    hourlyForecasts = hourlyForecasts
                )
            }
    }

    // Hàm để tính thang Beaufort dựa trên tốc độ gió (m/s)
    private fun calculateBeaufortScale(windSpeed: Float): Int {
        return when {
            windSpeed < 0.3f -> 0  // 0: Yên lặng
            windSpeed < 1.6f -> 1  // 1: Gió nhẹ
            windSpeed < 3.4f -> 2  // 2: Gió nhẹ
            windSpeed < 5.5f -> 3  // 3: Gió nhẹ nhàng
            windSpeed < 8.0f -> 4  // 4: Gió vừa
            windSpeed < 10.8f -> 5 // 5: Gió mạnh
            windSpeed < 13.9f -> 6 // 6: Gió rất mạnh
            windSpeed < 17.2f -> 7 // 7: Gió cao
            windSpeed < 20.8f -> 8 // 8: Gió bão
            windSpeed < 24.5f -> 9 // 9: Gió bão mạnh
            windSpeed < 28.5f -> 10 // 10: Bão
            windSpeed < 32.7f -> 11 // 11: Bão dữ dội
            else -> 12             // 12: Bão lớn
        }
    }
}