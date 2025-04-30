package com.example.textn.data.repository

import android.content.Context
import android.util.Log
import com.example.textn.data.model.WeatherResponse
import com.example.textn.data.api.WeatherApiService
import com.example.textn.utils.WeatherHelper

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
    // Viết thêm hàm này cho Worker
    suspend fun getWeatherData(context: Context, location: String): WeatherResponse {
        // Chuyển đổi địa chỉ thành tọa độ (latitude, longitude)
        val latLon = WeatherHelper.getCoordinatesFromLocation(context, location)

        if (latLon == null) {
            throw IllegalArgumentException("Không thể tìm tọa độ cho địa điểm '$location'.")
        }

        val lat = latLon.first
        val lon = latLon.second
        val apiKey = "32ea3752b81cf12722a46358a7a9739c"  // Đảm bảo đã thêm key trong file build.gradle

        return try {
            // Gọi API thời tiết với lat, lon và apiKey
            getWeather(lat, lon, apiKey)
        } catch (e: Exception) {
            throw IllegalStateException("Không thể lấy dữ liệu thời tiết từ API. Lỗi: ${e.message}")
        }
    }


}
