package com.example.textn.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.textn.data.model.WeatherResponse
import com.example.textn.data.api.WeatherApiService
import com.example.textn.utils.WeatherHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

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

    // Lấy vị trí hiện tại sử dụng FusedLocationProvider
     suspend fun getWeatherForCurrentLocation(context: Context): Location? = withContext(Dispatchers.IO) {
        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("WeatherRepository", "Không có quyền truy cập vị trí")
            return@withContext null
        }

        try {
            // Kiểm tra xem GPS hoặc network location có được kích hoạt
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                Log.e("WeatherRepository", "GPS và Network đều không được kích hoạt")
                return@withContext null
            }

            // Sử dụng FusedLocationProviderClient để lấy vị trí cuối cùng đã biết
            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            val locationTask = fusedLocationClient.lastLocation

            try {
                // Đợi tối đa 5 giây để lấy vị trí
                return@withContext Tasks.await(locationTask, 5, TimeUnit.SECONDS)
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Lỗi khi lấy vị trí: ${e.message}")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Lỗi khi truy cập dịch vụ vị trí: ${e.message}")
            return@withContext null
        }
    }

}
