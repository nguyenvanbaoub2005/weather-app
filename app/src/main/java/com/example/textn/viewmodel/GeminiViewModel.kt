package com.example.textn.viewmodel

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.example.textn.data.model.*
import com.example.textn.data.network.RetrofitClient
import com.example.textn.data.repository.WeatherRepository
import com.example.textn.utils.WeatherHelper
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
                // Lấy vị trí từ SharedPreferences hoặc vị trí hiện tại
                val location = getLocation()
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
                    Hãy đảm bảo kết thúc bằng một câu khích lệ tích cực, có dùng nhiều icon .
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

    // Hàm xử lý câu hỏi tùy chỉnh
    fun getCustomAdvice(customPrompt: String, apiKey: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Lấy vị trí và thời tiết hiện tại để bổ sung thông tin cho câu hỏi tùy chỉnh
                val location = getLocation()
                val weatherInfo = if (location != null) {
                    try {
                        val response = weatherRepository.getWeatherData(context, location)
                        val weatherData = convertWeatherResponseToWeatherData(response, location)
                        // Lấy dự báo 3 ngày
                        val forecast = response.daily.take(5).joinToString("\n") { day ->
                            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(day.dt * 1000))
                            "Ngày: $date - Nhiệt độ: ${day.temp.day}°C, ${day.weather[0].description}"
                        }
                        """
                    Thông tin thời tiết hiện tại tại $location:
                    - Nhiệt độ: ${weatherData.temperature}°C
                    - Chỉ số UV: ${weatherData.uvIndex}
                    - Độ ẩm: ${weatherData.humidity}%
                    - Tình trạng: ${weatherData.condition}
                    $forecast
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
                Nếu câu hỏi liên quan đến thời tiết, hãy sử dụng thông tin thời tiết đã cung cấp, dùng icon đẹp.
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

    // Hàm lấy vị trí từ SharedPreferences hoặc vị trí hiện tại
    private suspend fun getLocation(): String? {
        val sharedPrefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

        // Lấy vị trí đã lưu và thời gian cập nhật
        val savedLocation = sharedPrefs.getString("last_location", null)
        val lastUpdateTime = sharedPrefs.getLong("location_update_time", 0)
        val currentTime = System.currentTimeMillis()

        // Kiểm tra nếu vị trí còn mới (dưới 6 giờ)
        if (savedLocation != null && currentTime - lastUpdateTime < 6 * 60 * 60 * 1000) {
            Log.d("GeminiViewModel", "Sử dụng vị trí đã lưu: $savedLocation")
            return savedLocation
        }

        // Thử lấy vị trí hiện tại
        return try {
            getCurrentLocation()
        } catch (e: Exception) {
            Log.e("GeminiViewModel", "Không thể lấy vị trí hiện tại", e)
            // Nếu không lấy được vị trí mới, dùng vị trí cũ hoặc mặc định
            savedLocation ?: "Hồ Chí Minh" // Vị trí mặc định
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
            context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE).edit()
                .putString("last_location", locationName)
                .putLong("location_update_time", System.currentTimeMillis())
                .apply()

            Log.d("GeminiViewModel", "Đã cập nhật vị trí mới: $locationName")
            return locationName
        } catch (e: SecurityException) {
            Log.e("GeminiViewModel", "Không có quyền truy cập vị trí", e)
            return null
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
                val currentLocation = getLocation()
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
            
            Cho mỗi địa điểm: tên, khoảng cách ước tính, lý do phù hợp với thời tiết và một gợi ý hoạt động ngắn gọn,chỉ cho 3 địa điểm ,thêm nhiều icon.
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
                val currentLocation = getLocation()
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