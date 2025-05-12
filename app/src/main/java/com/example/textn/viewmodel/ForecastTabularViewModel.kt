package com.example.textn.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textn.data.model.ForecastTabularData
import com.example.textn.data.repository.ForecastRepository
import kotlinx.coroutines.launch

class ForecastTabularViewModel(
    private val repository: ForecastRepository = ForecastRepository.create()
) : ViewModel() {

    // LiveData để theo dõi dữ liệu dự báo
    private val _forecastData = MutableLiveData<ForecastTabularData>()
    val forecastData: LiveData<ForecastTabularData> = _forecastData

    // LiveData để theo dõi trạng thái tải
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData để theo dõi thông báo lỗi
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Phương thức để tải dữ liệu dự báo
     * @param lat Vĩ độ của địa điểm
     * @param lon Kinh độ của địa điểm
     * @param modelName Tên mô hình dự báo (mặc định là "GFS27")
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchForecastData(lat: Double, lon: Double, modelName: String = "GFS27") {
        // Đặt trạng thái tải và xóa thông báo lỗi trước khi bắt đầu
        _isLoading.value = true
        _errorMessage.value = null

        // Sử dụng coroutine để thực hiện tác vụ không đồng bộ
        viewModelScope.launch {
            try {
                // Gọi repository để lấy dữ liệu dự báo
                val result = repository.getForecastData(lat, lon)

                // Cập nhật LiveData với dữ liệu nhận được
                _forecastData.postValue(result)

                // Kết thúc quá trình tải
                _isLoading.postValue(false)
            } catch (e: Exception) {
                // Xử lý lỗi
                _isLoading.postValue(false)
                _errorMessage.postValue(e.localizedMessage ?: "Đã xảy ra lỗi khi tải dữ liệu")
                Log.e("ForecastTabularViewModel", "Fetch error", e)

            }
        }
    }
}