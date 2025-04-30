package com.example.textn.utils

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.textn.data.network.RetrofitClient
import com.example.textn.data.repository.WeatherRepository
import com.example.textn.data.services.AlertSeverity
import com.example.textn.data.services.HealthAlertAnalyzer
import com.example.textn.data.model.WeatherData
import com.example.textn.data.model.WeatherResponse
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class WeatherCheckWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = WeatherRepository(RetrofitClient.instance)
    private val healthAlertAnalyzer = HealthAlertAnalyzer()
    private val notificationHelper = NotificationHelper(context)
    private val sharedPrefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        return try {
            // Lấy vị trí từ SharedPreferences hoặc vị trí hiện tại
            val location = getLocation()
                ?: return Result.failure() // Nếu không lấy được vị trí thì trả về failure

            // Lấy dữ liệu thời tiết từ API
            val weatherResponse = repository.getWeatherData(context,location)

            // Chuyển đổi từ WeatherResponse sang WeatherData
            val weatherData = convertToWeatherData(weatherResponse, location)

            // Phân tích cảnh báo sức khỏe
            val alerts = healthAlertAnalyzer.analyzeWeatherData(weatherData)

            // Chỉ gửi thông báo với cảnh báo nghiêm trọng
            val highSeverityAlerts = alerts.filter { it.severity == AlertSeverity.HIGH }
            if (highSeverityAlerts.isNotEmpty()) {
                val title = if (highSeverityAlerts.size == 1) {
                    "Cảnh báo sức khỏe: ${highSeverityAlerts[0].title}"
                } else {
                    "${highSeverityAlerts.size} cảnh báo sức khỏe quan trọng"
                }

                val content = if (highSeverityAlerts.size == 1) {
                    highSeverityAlerts[0].description
                } else {
                    "Có nhiều cảnh báo sức khỏe cần chú ý tại $location"
                }

                notificationHelper.showNotification(title, content, highSeverityAlerts)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("WeatherCheckWorker", "Error during worker execution", e)
            Result.retry()
        }
    }

    // Hàm lấy vị trí từ SharedPreferences hoặc vị trí hiện tại
    private suspend fun getLocation(): String? {
        // Lấy vị trí đã lưu và thời gian cập nhật
        val savedLocation = sharedPrefs.getString("last_location", null)
        val lastUpdateTime = sharedPrefs.getLong("location_update_time", 0)
        val currentTime = System.currentTimeMillis()

        // Nếu vị trí còn mới (dưới 6 giờ)
        if (savedLocation != null && currentTime - lastUpdateTime < 6 * 60 * 60 * 1000) {
            Log.d("WeatherCheckWorker", "Sử dụng vị trí đã lưu: $savedLocation")
            return savedLocation
        }

        // Thử lấy vị trí hiện tại
        return try {
            getCurrentLocation()
        } catch (e: Exception) {
            Log.e("WeatherCheckWorker", "Không thể lấy vị trí hiện tại", e)
            // Nếu không lấy được vị trí mới, dùng vị trí cũ hoặc mặc định
            savedLocation ?: "Ho Chi Minh City" // Vị trí mặc định
        }
    }

    // Hàm lấy vị trí hiện tại (dùng Fused Location API)
    private suspend fun getCurrentLocation(): String? {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            // Kiểm tra quyền truy cập vị trí (trong Worker nên hạn chế yêu cầu quyền mới)
            val location: Location = fusedLocationClient.lastLocation.await() ?: return null

            // Chuyển tọa độ thành tên địa điểm
            val locationName = WeatherHelper.getLocationFromCoordinates(context, location.latitude, location.longitude)

            // Lưu vị trí vào SharedPreferences
            sharedPrefs.edit()
                .putString("last_location", locationName)
                .putLong("location_update_time", System.currentTimeMillis())
                .apply()

            Log.d("WeatherCheckWorker", "Đã cập nhật vị trí mới: $locationName")
            return locationName
        } catch (e: SecurityException) {
            Log.e("WeatherCheckWorker", "Không có quyền truy cập vị trí", e)
            return null
        }
    }

    // Hàm chuyển đổi từ WeatherResponse sang WeatherData
    private fun convertToWeatherData(response: WeatherResponse, location: String): WeatherData {
        return WeatherData(
            temperature = response.current.temp?.toFloat() ?: 0f,
            humidity = response.current.humidity?.toFloat() ?: 0f,
            condition = response.current.weather.firstOrNull()?.main ?: "Unknown",
            airQuality = response.current.airQuality?.aqi,
            uvIndex = response.current.uvi?.toFloat() ?: response.current.uvIndex?.toFloat() ?: 0f,
            location = location
        )
    }

    companion object {
        // Thiết lập lịch kiểm tra thời tiết định kỳ
        fun scheduleRecurringChecks(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WeatherCheckWorker>(3, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.LINEAR,
                    30,
                    TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "weather_check_worker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}
