package com.example.textn.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeather,
    val daily: List<DailyWeather>,
    val hourly: List<HourlyData>,
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

// Lớp dữ liệu để lưu trữ thông tin thời tiết theo giờ
data class HourlyData(
    val dt: Long, // Thời gian (timestamp Unix)
    val temp: Float, // Nhiệt độ
    @SerializedName("wind_speed")  // Sử dụng SerializedName nếu tên trường JSON khác
    val windSpeed: Float, // Tốc độ gió
    @SerializedName("wind_deg") // Hướng gió (độ)
    val windDeg: Int,
    @SerializedName("feels_like") // Nhiệt độ cảm nhận
    val feelsLike: Float,
    @SerializedName("wind_gust") // Tốc độ gió mạnh (có thể không có giá trị)
    val windGust: Float?,
    val pop: Double? // Xác suất mưa
)

// Lớp dữ liệu để lưu trữ thông tin thời tiết theo giờ
//data class HourlyData(
//    val dt: Long,                // Thời gian (Unix timestamp)
//    val temp: Float,             // Nhiệt độ
//    val windSpeed: Float,        // Tốc độ gió (đảm bảo ánh xạ đúng từ phản hồi của API)
//    val windDeg: Int,            // Hướng gió (độ)
//    val feelsLike: Float,        // Nhiệt độ cảm nhận
//    val windGust: Float?,        // Tốc độ gió mạnh (cần thiết cho những cơn gió mạnh)
//    val pop: Double?             // Xác suất mưa
//)

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
