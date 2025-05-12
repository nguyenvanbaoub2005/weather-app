package com.example.textn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.textn.data.repository.ForecastRepository

class ForecastTabularViewModelFactory(
    private val repository: ForecastRepository = ForecastRepository.create()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra xem modelClass có phải là ForecastTabularViewModel không
        if (modelClass.isAssignableFrom(ForecastTabularViewModel::class.java)) {
            return ForecastTabularViewModel(repository) as T
        }

        // Nếu không phải, ném ra ngoại lệ
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}