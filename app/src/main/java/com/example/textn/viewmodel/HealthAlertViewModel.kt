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
import com.example.textn.utils.FirebaseNotificationHelper
import com.example.textn.utils.NotificationHelper
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

    private val firebaseNotificationHelper = FirebaseNotificationHelper(application)
    private val localNotificationHelper = NotificationHelper(application)

    init {
        // Đăng ký nhận thông báo theo chủ đề
        firebaseNotificationHelper.subscribeToTopic("health_alerts")
        firebaseNotificationHelper.subscribeToTopic("weather_alerts")
    }

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
            id = "daytime_high_temp",
            severity = AlertSeverity.HIGH,
            title = "Cảnh báo: Nhiệt độ ban ngày cao",
            description = "Dự báo nhiệt độ ban ngày vượt quá 36°C — hãy tránh ra ngoài từ 11h đến 15h và uống nhiều nước.",
            iconResId = R.drawable.ic_high_temp_warning
        ),
        HealthAlert(
            id = "daytime_uv",
            severity = AlertSeverity.MEDIUM,
            title = "Cảnh báo: Chỉ số UV buổi trưa cao",
            description = "Chỉ số UV dự kiến đạt 12.0 vào buổi trưa — hãy sử dụng kem chống nắng, đội mũ và tránh tiếp xúc trực tiếp với ánh nắng.",
            iconResId = R.drawable.ic_uv_warning
        ),
        HealthAlert(
            id = "air_quality_moderate",
            severity = AlertSeverity.LOW,
            title = "Thông báo: Chất lượng không khí trung bình",
            description = "Chất lượng không khí (AQI: 110) vào buổi sáng có thể ảnh hưởng đến người nhạy cảm — hạn chế hoạt động ngoài trời.",
            iconResId = R.drawable.ic_air_quality_warning
        )
    )


    /**
     * Gửi Notification cho các cảnh báo có độ nghiêm trọng cao.
     * Sử dụng cả thông báo cục bộ và Firebase
     */
    private fun checkAndSendNotifications(alerts: List<HealthAlert>) {
        val highSeverity = alerts.filter { it.severity == AlertSeverity.HIGH }
        if (highSeverity.isNotEmpty()) {
            val title = if (highSeverity.size == 1)
                "Cảnh báo sức khỏe: ${highSeverity[0].title}"
            else
                "${highSeverity.size} cảnh báo sức khỏe quan trọng"

            val content = if (highSeverity.size == 1)
                highSeverity[0].description
            else
                "Có nhiều cảnh báo sức khỏe cần chú ý"

            // Hiển thị thông báo cục bộ
            localNotificationHelper.showNotification(title, content, highSeverity)

            // Gửi thông báo qua Firebase nếu cần đồng bộ với các thiết bị khác
            sendFirebaseAlert(title, content, highSeverity)
        }
    }

    /**
     * Gửi thông báo cảnh báo sức khỏe qua Firebase (Demo - thực tế cần server)
     * Trong triển khai thực tế, việc này nên được thực hiện từ server backend
     */
    private fun sendFirebaseAlert(title: String, content: String, alerts: List<HealthAlert>) {
        // Trong ứng dụng thực tế, bạn sẽ gửi yêu cầu đến server của mình
        // và server sẽ gửi thông báo FCM tới các thiết bị đã đăng ký

        // Đây chỉ là demo để hiển thị cách thức hoạt động
        // Trong môi trường thực tế, KHÔNG nên gửi thông báo FCM từ client

        Log.d("HealthAlertViewModel", "Đã phát hiện cảnh báo nghiêm trọng, " +
                "trong triển khai thực tế, server sẽ gửi thông báo FCM đến tất cả thiết bị đã đăng ký")

        // Đối với các thiết bị khác của cùng người dùng,
        // bạn có thể sử dụng user-specific topic
        // Ví dụ: "user_123_health_alerts"
    }
}
