package com.example.textn.data.services

import android.util.Log
import com.example.textn.R
import com.example.textn.data.model.HealthAlert
import com.example.textn.data.model.WeatherData

/**
 * Phân tích cảnh báo sức khỏe dựa trên dữ liệu thời tiết.
 * Ghi log chi tiết dữ liệu đầu vào và đầu ra để theo dõi.
 */
class HealthAlertAnalyzer {

    companion object {
        private const val TAG = "HealthAlertAnalyzer"
    }

    /**
     * Entry-point: Phân tích và trả về danh sách cảnh báo.
     */
    fun analyzeWeatherData(weatherData: WeatherData): List<HealthAlert> {
        Log.d(TAG, "Received WeatherData: $weatherData")
        val alerts = mutableListOf<HealthAlert>()

        analyzeTemperature(weatherData.temperature, alerts)
        analyzeHumidity(weatherData.humidity, weatherData.temperature, alerts)
        weatherData.airQuality?.let { analyzeAirQuality(it, alerts) }
        weatherData.uvIndex?.let { analyzeUvIndex(it, alerts) }
        analyzeWeatherCondition(weatherData.condition, alerts)

        Log.d(TAG, "Generated \${alerts.size} alerts: \$alerts")
        return alerts
    }

    private fun analyzeTemperature(temperature: Float, alerts: MutableList<HealthAlert>) {
        when {
            temperature >= 35 -> {
                val tempText = "Nhiệt độ ${temperature}°C có thể gây say nắng. Hạn chế ra ngoài, uống nhiều nước."
                val alert = HealthAlert(
                    id = "high_temp",
                    severity = AlertSeverity.HIGH,
                    title = "Nguy cơ say nắng cao",
                    description = tempText,
                    iconResId = R.drawable.ic_high_temp_warning
                )
                Log.d(TAG, "Added temperature alert: $alert")
                alerts.add(alert)
            }
            temperature >= 30 -> {
                val tempText = "Nhiệt độ ${temperature}°C, nên uống đủ nước và tránh hoạt động gắng sức ngoài trời."
                val alert = HealthAlert(
                    id = "warm_temp",
                    severity = AlertSeverity.MEDIUM,
                    title = "Nhiệt độ cao",
                    description = tempText,
                    iconResId = R.drawable.ic_temp_warning
                )
                Log.d(TAG, "Added temperature alert: $alert")
                alerts.add(alert)
            }
            temperature <= 10 -> {
                val tempText = "Nhiệt độ ${temperature}°C, nên mặc đủ ấm để tránh hạ thân nhiệt."
                val alert = HealthAlert(
                    id = "low_temp",
                    severity = AlertSeverity.MEDIUM,
                    title = "Nhiệt độ thấp",
                    description = tempText,
                    iconResId = R.drawable.ic_cold_warning
                )
                Log.d(TAG, "Added temperature alert: $alert")
                alerts.add(alert)
            }
        }
    }

    private fun analyzeHumidity(humidity: Float, temperature: Float, alerts: MutableList<HealthAlert>) {
        if (humidity > 80 && temperature > 28) {
            val humidityText = "Độ ẩm ${humidity}% kết hợp nhiệt độ ${temperature}°C có thể gây khó thở và mệt mỏi. Hạn chế hoạt động gắng sức."
            val alert = HealthAlert(
                id = "high_humidity",
                severity = AlertSeverity.MEDIUM,
                title = "Độ ẩm cao kết hợp nhiệt độ nóng",
                description = humidityText,
                iconResId = R.drawable.ic_humidity_warning
            )
            Log.d(TAG, "Added humidity alert: $alert")
            alerts.add(alert)
        }
    }

    private fun analyzeAirQuality(airQuality: Int, alerts: MutableList<HealthAlert>) {
        when {
            airQuality > 150 -> {
                val airQualityText = "Chỉ số chất lượng không khí ${airQuality} không tốt cho sức khỏe. Người có bệnh hô hấp nên ở trong nhà."
                val alert = HealthAlert(
                    id = "bad_air",
                    severity = AlertSeverity.HIGH,
                    title = "Chất lượng không khí kém",
                    description = airQualityText,
                    iconResId = R.drawable.ic_air_quality_warning
                )
                Log.d(TAG, "Added air quality alert: $alert")
                alerts.add(alert)
            }
            airQuality > 100 -> {
                val airQualityText = "Chỉ số chất lượng không khí ${airQuality} có thể gây khó chịu cho người nhạy cảm. Hạn chế hoạt động ngoài trời kéo dài."
                val alert = HealthAlert(
                    id = "moderate_air",
                    severity = AlertSeverity.MEDIUM,
                    title = "Chất lượng không khí trung bình",
                    description = airQualityText,
                    iconResId = R.drawable.ic_air_quality_warning
                )
                Log.d(TAG, "Added air quality alert: $alert")
                alerts.add(alert)
            }
        }
    }

    private fun analyzeUvIndex(uvIndex: Float, alerts: MutableList<HealthAlert>) {
        when {
            uvIndex >= 8 -> {
                val uvText = "Chỉ số UV ${uvIndex} có thể gây bỏng da trong vài phút. Tránh ra ngoài nắng từ 10h-16h."
                val alert = HealthAlert(
                    id = "high_uv",
                    severity = AlertSeverity.HIGH,
                    title = "Chỉ số UV rất cao",
                    description = uvText,
                    iconResId = R.drawable.ic_uv_warning
                )
                Log.d(TAG, "Added UV alert: $alert")
                alerts.add(alert)
            }
            uvIndex >= 6 -> {
                val uvText = "Chỉ số UV ${uvIndex} cao, cần bôi kem chống nắng và đội mũ khi ra ngoài."
                val alert = HealthAlert(
                    id = "moderate_uv",
                    severity = AlertSeverity.MEDIUM,
                    title = "Chỉ số UV cao",
                    description = uvText,
                    iconResId = R.drawable.ic_uv_warning
                )
                Log.d(TAG, "Added UV alert: $alert")
                alerts.add(alert)
            }
        }
    }

    private fun analyzeWeatherCondition(condition: String, alerts: MutableList<HealthAlert>) {
        when (condition.lowercase()) {
            "rain", "thunderstorm" -> {
                val conditionText = "Thời tiết ${condition} có thể ảnh hưởng đến người có bệnh hô hấp."
                val alert = HealthAlert(
                    id = "rain",
                    severity = AlertSeverity.LOW,
                    title = "Thời tiết mưa",
                    description = conditionText,
                    iconResId = R.drawable.ic_rain_warning
                )
                Log.d(TAG, "Added condition alert: $alert")
                alerts.add(alert)
            }
            "dust", "haze", "smoke", "fog" -> {
                val conditionText = "Tầm nhìn hạn chế do ${condition}, người có bệnh hô hấp nên hạn chế ra ngoài."
                val alert = HealthAlert(
                    id = "poor_visibility",
                    severity = AlertSeverity.MEDIUM,
                    title = "Tầm nhìn hạn chế",
                    description = conditionText,
                    iconResId = R.drawable.ic_visibility_warning
                )
                Log.d(TAG, "Added condition alert: $alert")
                alerts.add(alert)
            }
        }
    }
}

enum class AlertSeverity {
    LOW, MEDIUM, HIGH
}
