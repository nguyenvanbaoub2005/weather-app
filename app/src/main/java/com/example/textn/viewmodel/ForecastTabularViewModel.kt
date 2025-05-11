package com.example.textn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textn.data.model.ForecastTabularData
import com.example.textn.data.model.HourlyForecast
import com.example.textn.data.repository.ForecastRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ForecastTabularViewModel(private val repository: ForecastRepository) : ViewModel() {

    private val _forecastData = MutableLiveData<ForecastTabularData>()
    val forecastData: LiveData<ForecastTabularData> = _forecastData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchForecastData(latitude: Double, longitude: Double, model: String = "GFS27") {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = repository.getForecastData(latitude, longitude, model)
                _forecastData.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load forecast data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFormattedDateForPosition(position: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, position)
        val dayFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        return dayFormat.format(calendar.time)
    }

    fun getHourlyTimestamps(): List<String> {
        return listOf("7 AM", "10 AM", "1 PM", "4 PM", "7 PM", "10 PM", "1 AM", "4 AM")
    }
}