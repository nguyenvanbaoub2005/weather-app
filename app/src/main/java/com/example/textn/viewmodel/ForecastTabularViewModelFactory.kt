package com.example.textn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.textn.data.api.WeatherApiService
import com.example.textn.data.repository.ForecastRepository

class ForecastTabularViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForecastTabularViewModel::class.java)) {
            // Create instance of WeatherApiService
            val apiService = WeatherApiService.create()

            // Create repository
            val repository = ForecastRepository(apiService)

            // Return view model
            return ForecastTabularViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}