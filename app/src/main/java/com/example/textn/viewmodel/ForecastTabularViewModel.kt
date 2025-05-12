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
    // Hàm để lấy dữ liệu dự báo thời tiết dựa trên vĩ độ và kinh độ
    fun fetchForecastData(lat: Double, lon: Double, modelName: String = "GFS27") {
        _isLoading.value = true // Đánh dấu đang trong quá trình tải dữ liệu
        _errorMessage.value = null // Xóa thông báo lỗi trước đó

        // Sử dụng coroutine để thực hiện tải dữ liệu không đồng bộ
        viewModelScope.launch {
            try {
                // Gọi hàm để lấy dữ liệu dự báo từ repository
                val result = repository.getForecastData(lat, lon)
                _forecastData.postValue(result) // Cập nhật dữ liệu dự báo
                _isLoading.postValue(false) // Đánh dấu quá trình tải hoàn tất
            } catch (e: Exception) {
                _isLoading.postValue(false) // Đánh dấu quá trình tải hoàn tất trong trường hợp có lỗi
                _errorMessage.postValue(e.localizedMessage ?: "Đã xảy ra lỗi khi tải dữ liệu") // Cập nhật thông báo lỗi
                Log.e("ForecastTabularViewModel", "Fetch error", e) // Ghi log lỗi
            }
        }
    }
}