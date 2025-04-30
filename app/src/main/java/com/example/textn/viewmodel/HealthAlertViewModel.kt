package com.example.textn.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.textn.R
import com.example.textn.data.model.HealthAlert
import com.example.textn.data.model.WeatherData
import com.example.textn.data.repository.WeatherRepository
import com.example.textn.data.services.AlertSeverity
import com.example.textn.data.services.HealthAlertAnalyzer
import com.example.textn.utils.NotificationHelper
import com.example.textn.utils.WeatherHelper
import kotlinx.coroutines.launch

class HealthAlertViewModel(
    application: Application,
    private val repository: WeatherRepository,
    private val healthAlertAnalyzer: HealthAlertAnalyzer = HealthAlertAnalyzer()
) : AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData

    private val _healthAlerts = MutableLiveData<List<HealthAlert>>()
    val healthAlerts: LiveData<List<HealthAlert>> = _healthAlerts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    /**
     * Lấy dữ liệu thời tiết theo location.
     * Nếu fetch thất bại hoặc không có cảnh báo, sẽ dùng dữ liệu mẫu.
     */
    fun fetchWeatherData(location: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Gọi phương thức trong repository để lấy dữ liệu thời tiết
                val response = repository.getWeatherData(getApplication(), location)

                // Kiểm tra dữ liệu thời tiết từ API
                if (response.current == null) {
                    throw Exception("Dữ liệu thời tiết không hợp lệ. Kiểm tra lại kết nối mạng hoặc API.")
                }

                // Map sang model UI
                val wd = WeatherData(
                    temperature = response.current.temp.toFloat(),
                    humidity    = response.current.humidity.toFloat(),
                    condition   = response.current.weather.firstOrNull()?.description ?: "Không rõ",
                    airQuality  = response.current.airQuality?.aqi,
                    uvIndex     = response.current.uvi?.toFloat(),
                    location    = location
                )
                _weatherData.value = wd

                // Phân tích cảnh báo
                val alerts = healthAlertAnalyzer.analyzeWeatherData(wd)
                // Trả về cảnh báo thực tế nếu có, nếu không thì dùng cảnh báo mẫu
                _healthAlerts.value = if (alerts.isNotEmpty()) alerts else getSampleAlerts()

                // Gửi thông báo cảnh báo nghiêm trọng
                checkAndSendNotifications(alerts)

            } catch (e: Exception) {
                // Ghi lại thông tin lỗi vào log để giúp kiểm tra sau
                Log.e("WeatherDataError", "Lỗi khi tải dữ liệu thời tiết: ${e.message}", e)
                // Thông báo lỗi và trả về dữ liệu mẫu
                _error.value = e.message ?: "Lỗi khi tải dữ liệu thời tiết. Vui lòng kiểm tra lại kết nối hoặc thử lại sau."
                _healthAlerts.value = getSampleAlerts()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cung cấp danh sách cảnh báo mẫu.
     */
    private fun getSampleAlerts(): List<HealthAlert> = listOf(
        HealthAlert(
            id = "sample_high_temp",
            severity = AlertSeverity.HIGH,
            title = "Mẫu: Nhiệt độ cao",
            description = "Nhiệt độ trên 35°C — hãy hạn chế ra ngoài và uống đủ nước.",
            iconResId = R.drawable.ic_high_temp_warning
        ),
        HealthAlert(
            id = "sample_uv",
            severity = AlertSeverity.MEDIUM,
            title = "Mẫu: Chỉ số UV cao",
            description = "Chỉ số UV 7.5 — hãy bôi kem chống nắng.",
            iconResId = R.drawable.ic_uv_warning
        ),
        HealthAlert(
            id = "sample_air",
            severity = AlertSeverity.LOW,
            title = "Mẫu: Ô nhiễm không khí",
            description = "AQI 120 — hạn chế ra ngoài nếu khó thở.",
            iconResId = R.drawable.ic_air_quality_warning
        )
    )

    /**
     * Gửi Notification cho các cảnh báo có độ nghiêm trọng cao.
     */
    private fun checkAndSendNotifications(alerts: List<HealthAlert>) {
        val highSeverity = alerts.filter { it.severity == AlertSeverity.HIGH }
        if (highSeverity.isNotEmpty()) {
            val notificationHelper = NotificationHelper(getApplication())
            val title = if (highSeverity.size == 1)
                "Cảnh báo sức khỏe: ${highSeverity[0].title}"
            else
                "${highSeverity.size} cảnh báo sức khỏe quan trọng"
            val content = if (highSeverity.size == 1)
                highSeverity[0].description
            else
                "Có nhiều cảnh báo sức khỏe cần chú ý"
            notificationHelper.showNotification(title, content, highSeverity)
        }
    }
}
