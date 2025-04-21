package com.example.textn.data.repository

import com.example.textn.data.model.WeatherResponse
import com.example.textn.data.network.WeatherApiService

class WeatherRepository(private val apiService: WeatherApiService) {

    suspend fun getWeather(
        lat: Double,
        lon: Double,
        apiKey: String
    ): WeatherResponse {
        val response = apiService.getWeatherForecast(lat, lon, apiKey)

        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Dữ liệu trả về rỗng")
        } else {
            throw Exception("Lỗi từ server: ${response.code()} - ${response.message()}")
        }
    }
}
