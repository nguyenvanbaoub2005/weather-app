package com.example.textn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.textn.model.LocationData
import com.example.textn.repository.LocationRepository

class SearchViewModel(private val repository: LocationRepository) : ViewModel() {

    private val _searchResults = MutableLiveData<List<LocationData>>()
    val searchResults: LiveData<List<LocationData>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun searchCities(query: String) {
        _isLoading.value = true
        _error.value = null

        try {
            val results = repository.searchCities(query)
            _searchResults.value = results
        } catch (e: Exception) {
            _error.value = "Error searching for cities: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
