package com.example.textn.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.textn.model.LocationData
import com.example.textn.repository.LocationRepository
import com.example.textn.utils.WeatherHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(
    private val repository: LocationRepository,
    private val context: Context
) : ViewModel() {
    private val _searchResults = MutableLiveData<List<LocationData>>()
    val searchResults: LiveData<List<LocationData>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _coordinates = MutableLiveData<Pair<Double, Double>?>()
    val coordinates: LiveData<Pair<Double, Double>?> = _coordinates

    private val _customLocation = MutableLiveData<LocationData?>()
    val customLocation: LiveData<LocationData?> = _customLocation

    // Flag to indicate if we're performing search from direct submission
    private var isDirectSubmit = false

    // For debouncing search requests
    private var searchJob: Job? = null
    private val searchScope = CoroutineScope(Dispatchers.Main)

    // Main search function that handles both typing and submit events
    fun searchCities(query: String, isSubmit: Boolean = false) {
        // Cancel any ongoing search
        searchJob?.cancel()

        // Update the flag based on how search was triggered
        isDirectSubmit = isSubmit

        // Don't show loading indicator for every keystroke
        if (isSubmit) {
            _isLoading.value = true
        }

        // Clear previous errors
        _error.value = null

        // For empty queries, reset to default Vietnam list
        if (query.isBlank()) {
            handleEmptyQuery()
            return
        }

        // Use debouncing for better performance
        searchJob = searchScope.launch {
            // Add delay for typing (not for submit)
            if (!isSubmit) {
                delay(300) // Wait 300ms before processing typing input
            }

            try {
                performSearch(query)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun performSearch(query: String) = withContext(Dispatchers.IO) {
        try {
            // First search in local repository
            val results = repository.searchCities(query)

            withContext(Dispatchers.Main) {
                _searchResults.value = results.take(5)


                // Only try Geocoder on direct submission or if no results and specifically requested
                if (results.isEmpty() && isDirectSubmit) {
                    getCoordinatesAndCreateLocation(query)
                } else {
                    // Clear custom location if we found results in the repository
                    _customLocation.value = null
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                if (isDirectSubmit) {
                    _error.value = "Lỗi tìm kiếm thành phố: ${e.message}"
                }
            }
        }
    }

    private fun handleEmptyQuery() {
        searchJob?.cancel()
        _isLoading.value = false
        _customLocation.value = null
        searchJob = searchScope.launch {
            val defaultResults = repository.searchCities("Vietnam")
            _searchResults.value = defaultResults
        }
    }

    private fun getCoordinatesAndCreateLocation(locationName: String) {
        try {
            // Get coordinates using WeatherHelper
            val coords = WeatherHelper.getCoordinatesFromLocation(context, locationName)

            coords?.let { (lat, lon) ->
                _coordinates.value = coords

                // Try to get a more accurate location name from coordinates
                val actualName = try {
                    WeatherHelper.getLocationFromCoordinates(context, lat, lon)
                } catch (e: Exception) {
                    locationName // Use original name if reverse geocoding fails
                }

                // Create new LocationData
                val newLocation = LocationData(
                    name = actualName,
                    latitude = lat,
                    longitude = lon,
                    rating = 4.5f,
                    description = "Địa điểm tìm kiếm"
                )
                _customLocation.value = newLocation
            } ?: run {
                if (isDirectSubmit) {
                    _error.value = "Không tìm thấy địa điểm: $locationName"
                }
                _customLocation.value = null
            }
        } catch (e: Exception) {
            if (isDirectSubmit) {
                _error.value = "Lỗi khi tìm tọa độ: ${e.message}"
            }
            _customLocation.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }
}