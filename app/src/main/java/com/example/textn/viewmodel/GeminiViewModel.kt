package com.example.textn.viewmodel

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.example.textn.data.model.*
import com.example.textn.data.network.RetrofitClient
import com.example.textn.data.repository.WeatherRepository
import com.example.textn.utils.WeatherHelper
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeminiViewModel(private val context: Context) : ViewModel() {

    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> get() = _aiResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> get() = _weatherData

    private val weatherRepository = WeatherRepository(RetrofitClient.instance)

    // ----------------- Lấy thời tiết từ thiết bị và gửi yêu cầu đến AI -----------------
    fun getWeatherForCurrentLocationAndAdvice(apiKey: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Lấy vị trí hiện tại
                val location = getCurrentLocation()
                if (location == null) {
                    _error.postValue("Không thể lấy vị trí hiện tại.")
                    _isLoading.postValue(false)
                    return@launch
                }

                // Lấy dữ liệu thời tiết từ API
                val response = weatherRepository.getWeatherData(context, location)
                val weatherData = convertWeatherResponseToWeatherData(response, location)

                // Lưu dữ liệu thời tiết
                _weatherData.postValue(weatherData)

                // Gửi yêu cầu đến AI để nhận lời khuyên
                val temperature = weatherData.temperature
                val uvIndex = weatherData.uvIndex
                val humidity = weatherData.humidity
                val condition = weatherData.condition

                val prompt = """
                    Tôi đang ở $location. Thời tiết hiện tại:
                    - Nhiệt độ: ${temperature}°C
                    - Chỉ số UV: $uvIndex
                    - Độ ẩm: $humidity%
                    - Tình trạng: $condition
                    
                    Hãy đóng vai là bác sĩ và đưa ra lời khuyên ngắn gọn về sức khỏe phù hợp với điều kiện thời tiết này. 
                    Hãy đề cập đến các biện pháp phòng ngừa cần thiết và những hoạt động nên tránh hoặc nên làm.
                    Hãy đảm bảo kết thúc bằng một câu khích lệ tích cực, dùng nhiều icon, rút ngắn ý lại.
                """.trimIndent()

                // Gọi hàm gửi yêu cầu tới API Gemini để nhận lời khuyên
                getWeatherAdviceFromAI(prompt, apiKey)

                Log.d("GeminiViewModel", "Đã lấy thời tiết tại: ${weatherData.location}")
            } catch (e: Exception) {
                Log.e("GeminiViewModel", "Lỗi khi lấy thời tiết: ${e.message}")
                _error.postValue("Không thể lấy thời tiết hiện tại: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    // Hàm xử lý câu hỏi tùy chỉnh cho AI
    fun getCustomAdvice(customPrompt: String, apiKey: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Lấy vị trí và thời tiết hiện tại để bổ sung thông tin cho câu hỏi tùy chỉnh
                val location = getCurrentLocation()
                val weatherInfo = if (location != null) {
                    try {
                        val response = weatherRepository.getWeatherData(context, location)
                        val weatherData = convertWeatherResponseToWeatherData(response, location)
                        // Lấy dự báo 5 ngày
                        val forecastDaily = response.daily.take(5).joinToString("\n") { day ->
                            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(day.dt * 1000))
                            "Ngày: $date - Nhiệt độ: ${day.temp.day}°C, ${day.weather[0].description}"
                        }
                        // Lấy dự báo theo giờ (ví dụ: 5 giờ tiếp theo)
                        val forecastHourly = response.hourly.take(5).joinToString("\n") { hour ->
                            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(hour.dt * 1000))
                            "Giờ: $time - Nhiệt độ: ${hour.temp}°C, Gió: ${hour.windSpeed} m/s, Xác suất mưa: ${(hour.pop ?: 0.0) * 100}%"
                        }

                        // Lấy thông tin chất lượng không khí từ CurrentWeather
                        val airQualityInfo = response.current.airQuality?.aqi?.let { aqi ->
                            when (aqi) {
                                in 0..50 -> "Tốt ($aqi)"
                                in 51..100 -> "Trung bình ($aqi)"
                                in 101..150 -> "Không tốt cho nhóm nhạy cảm ($aqi)"
                                in 151..200 -> "Không tốt ($aqi)"
                                in 201..300 -> "Rất không tốt ($aqi)"
                                else -> "Nguy hiểm ($aqi)"
                            }
                        } ?: "Không có dữ liệu"

                        """
                    🌤️ Thông tin thời tiết hiện tại tại $location:
                    - Nhiệt độ: ${weatherData.temperature}°C
                    - Chỉ số UV: ${weatherData.uvIndex}
                    - Độ ẩm: ${weatherData.humidity}%
                    - Tình trạng: ${weatherData.condition}
                    - Chất lượng không khí (AQI): $airQualityInfo
                    
                    🔮 Dự báo 5 ngày tới:
                    $forecastDaily
                    
                    ⏰ Dự báo theo giờ (5 giờ tới):
                    $forecastHourly
                    """.trimIndent()
                    } catch (e: Exception) {
                        "Không có thông tin thời tiết hiện tại hoặc dự báo."
                    }
                } else {
                    "Không có thông tin thời tiết hiện tại hoặc dự báo."
                }

                // Tạo prompt kết hợp câu hỏi của người dùng và thông tin thời tiết
                val prompt = """
                $weatherInfo
                
                Câu hỏi của người dùng: $customPrompt
                
                Hãy trả lời câu hỏi trên một cách ngắn gọn, rõ ràng và cung cấp thông tin hữu ích. 
                Nếu câu hỏi liên quan đến thời tiết hoặc chất lượng không khí, hãy sử dụng thông tin đã cung cấp, dùng icon đẹp.
            """.trimIndent()
                // Gửi yêu cầu đến API Gemini
                getWeatherAdviceFromAI(prompt, apiKey)
            } catch (e: Exception) {
                _error.postValue("Lỗi khi xử lý câu hỏi: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
    // Hàm gửi yêu cầu đến API Gemini
    private fun getWeatherAdviceFromAI(prompt: String, apiKey: String) {
        _isLoading.value = true

        val requestBody = GeminiRequestBody(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 1024
            )
        )

        RetrofitClient.geminiApiService.getResponseFromGemini(apiKey, requestBody)
            .enqueue(object : Callback<GeminiResponse> {
                override fun onResponse(
                    call: Call<GeminiResponse>,
                    response: Response<GeminiResponse>
                ) {
                    _isLoading.postValue(false)

                    if (response.isSuccessful) {
                        val message = response.body()
                            ?.candidates?.firstOrNull()
                            ?.content?.parts?.firstOrNull()
                            ?.text

                        if (!message.isNullOrBlank()) {
                            _aiResponse.postValue(message.trim())
                            Log.d("GeminiAPI", "Đã nhận phản hồi thành công")
                        } else {
                            _error.postValue("Không có nội dung phản hồi từ AI.")
                            Log.e("GeminiAPI", "Phản hồi rỗng")
                        }
                    } else {
                        val errorMsg = "Lỗi: ${response.code()} - ${response.errorBody()?.string()}"
                        _error.postValue(errorMsg)
                        Log.e("GeminiAPI", errorMsg)
                    }
                }

                override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                    _isLoading.postValue(false)
                    val errorMsg = "Lỗi kết nối: ${t.localizedMessage}"
                    _error.postValue(errorMsg)
                    Log.e("GeminiAPI", "Yêu cầu thất bại", t)
                }
            })
    }



    // Hàm lấy vị trí hiện tại (dùng Fused Location API)
    suspend fun getCurrentLocation(): String? {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            // Kiểm tra quyền truy cập vị trí (trong Worker nên hạn chế yêu cầu quyền mới)
            val location: Location = fusedLocationClient.lastLocation.await() ?: return null

            // Chuyển tọa độ thành tên phường
            val wardName = getWardName(context, location.latitude, location.longitude)

            // Lưu vị trí vào SharedPreferences
            context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE).edit()
                .putString("last_location", wardName)
                .putLong("location_update_time", System.currentTimeMillis())
                .apply()

            Log.d("GeminiViewModel", "Đã cập nhật vị trí mới: $wardName")
            return wardName
        } catch (e: SecurityException) {
            Log.e("GeminiViewModel", "Không có quyền truy cập vị trí", e)
            return null
        }
    }

    // Hàm mới để lấy tên phường từ tọa độ
    private suspend fun getWardName(context: Context, latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]

                    // Ưu tiên lấy phường từ addressLine
                    val wardName = extractWardName(address.getAddressLine(0))
                    if (wardName != null) {
                        return@withContext wardName
                    }

                    // Nếu không tìm thấy, thử lấy thông tin từ subLocality
                    val subLocality = address.subLocality  // Thường là phường/xã
                    if (subLocality != null) {
                        return@withContext extractWardNameFromText(subLocality)
                    }

                    // Fallback nếu không tìm được phường
                    return@withContext "Unknown Location"
                } else {
                    return@withContext "Unknown Location"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Unknown Location"
            }
        }
    }

    // Hàm trích xuất tên phường từ chuỗi địa chỉ
    private fun extractWardName(addressLine: String?): String? {
        if (addressLine.isNullOrEmpty()) return null

        val parts = addressLine.split(", ")

        // Tìm phường trong địa chỉ
        val wardPart = parts.find {
            it.contains("Phường", ignoreCase = true) ||
                    it.contains("Xã", ignoreCase = true) ||
                    it.contains("Thị trấn", ignoreCase = true)
        }

        return wardPart?.let { extractWardNameFromText(it) }
    }

    // Hàm loại bỏ tiền tố "Phường", "Xã", "Thị trấn"
    private fun extractWardNameFromText(text: String): String {
        return when {
            text.startsWith("Phường ", ignoreCase = true) ->
                text.substring("Phường ".length).trim()
            text.startsWith("Xã ", ignoreCase = true) ->
                text.substring("Xã ".length).trim()
            text.startsWith("Thị trấn ", ignoreCase = true) ->
                text.substring("Thị trấn ".length).trim()
            else -> text
        }
    }

//    /*
//     * Chuyển đổi từ WeatherResponse → WeatherData
//     */
    private fun convertWeatherResponseToWeatherData(
        response: WeatherResponse,
        location: String
    ): WeatherData {
        return WeatherData(
            temperature = response.current.temp?.toFloat() ?: 0f,
            humidity = response.current.humidity?.toFloat() ?: 0f,
            condition = response.current.weather.firstOrNull()?.main ?: "Unknown",
            airQuality = response.current.airQuality?.aqi,
            uvIndex = response.current.uvi?.toFloat() ?: response.current.uvIndex?.toFloat() ?: 0f,
            location = location
        )
    }
    // Hàm gợi ý vị trí gần vị trí hiện tại bằng AI Gemini
    fun getSuggestedLocationsNearbyEntertaiment(apiKey: String, numberOfLocations: Int = 5, locationType: String = "all") {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Lấy vị trí hiện tại
                val currentLocation = getCurrentLocation()
                if (currentLocation == null) {
                    _error.postValue("Không thể lấy vị trí hiện tại.")
                    _isLoading.postValue(false)
                    return@launch
                }

                // Lấy thông tin thời tiết hiện tại (nếu có)
                val weatherInfo = try {
                    val response = weatherRepository.getWeatherData(context, currentLocation)
                    val weatherData = convertWeatherResponseToWeatherData(response, currentLocation)
                    "Thời tiết: ${weatherData.temperature}°C, ${weatherData.condition}, độ ẩm ${weatherData.humidity}%"
                } catch (e: Exception) {
                    "Không có thông tin thời tiết."
                }

                // Xác định loại địa điểm cần gợi ý
                val locationTypePrompt = when (locationType.lowercase(Locale.getDefault())) {
                    "food" -> "ăn uống"
                    "entertainment" -> "giải trí"
                    "shopping" -> "mua sắm"
                    "accommodation" -> "lưu trú"
                    else -> "du lịch, ăn uống, giải trí hoặc mua sắm"
                }

                // Tạo prompt ngắn gọn cho AI
                val prompt = """
            Vị trí hiện tại: $currentLocation
            $weatherInfo
            
            Gợi ý $numberOfLocations địa điểm $locationTypePrompt gần đây phù hợp với thời tiết hiện tại.
            
            Cho mỗi địa điểm: tên, khoảng cách ước tính, lý do phù hợp với thời tiết và một gợi ý hoạt động ngắn gọn,chỉ cho 3 địa điểm ,thêm nhiều icon,kết thúc bằng một câu chúc hay.
            """.trimIndent()

                // Gọi hàm gửi yêu cầu tới API Gemini
                getWeatherAdviceFromAI(prompt, apiKey)

                Log.d("GeminiViewModel", "Đã gửi yêu cầu gợi ý địa điểm gần $currentLocation")
            } catch (e: Exception) {
                Log.e("GeminiViewModel", "Lỗi khi lấy gợi ý địa điểm: ${e.message}")
                _error.postValue("Không thể lấy gợi ý địa điểm: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
    fun getSuggestedLocationsNearby(apiKey: String, numberOfLocations: Int = 5) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Lấy vị trí hiện tại
                val currentLocation = getCurrentLocation()
                if (currentLocation == null) {
                    _error.postValue("Không thể lấy vị trí hiện tại.")
                    _isLoading.postValue(false)
                    return@launch
                }

                // Lấy thông tin thời tiết hiện tại (nếu có)
                val weatherInfo = try {
                    val response = weatherRepository.getWeatherData(context, currentLocation)
                    val weatherData = convertWeatherResponseToWeatherData(response, currentLocation)
                    "Thời tiết: ${weatherData.temperature}°C, ${weatherData.condition}, độ ẩm ${weatherData.humidity}%"
                } catch (e: Exception) {
                    "Không có thông tin thời tiết."
                }

                // Tạo prompt đơn giản để lấy địa điểm gần vị trí hiện tại
                val prompt = """
            Vị trí hiện tại: $currentLocation
            $weatherInfo
            
            Gợi ý $numberOfLocations địa điểm đáng chú ý gần vị trí hiện tại của tôi.
            
            Cho mỗi địa điểm hãy cung cấp:
            - Tên địa điểm
            - Khoảng cách ước tính từ vị trí hiện tại
            - Mô tả ngắn về địa điểm đó và lý do nên ghé thăm
            """.trimIndent()

                // Gọi hàm gửi yêu cầu tới API Gemini
                getWeatherAdviceFromAI(prompt, apiKey)
                Log.d("GeminiViewModel", "Đã gửi yêu cầu gợi ý địa điểm gần $currentLocation")
            } catch (e: Exception) {
                Log.e("GeminiViewModel", "Lỗi khi lấy gợi ý địa điểm: ${e.message}")
                _error.postValue("Không thể lấy gợi ý địa điểm: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
}