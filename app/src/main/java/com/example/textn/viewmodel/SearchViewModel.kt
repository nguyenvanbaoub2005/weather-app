package com.example.textn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.textn.model.ActivityType
import com.example.textn.model.LocationSearchItem
import com.example.textn.model.LocationType
import kotlinx.coroutines.*

class SearchViewModel : ViewModel() {

    private val _searchResults = MutableLiveData<List<LocationSearchItem>>()
    val searchResults: LiveData<List<LocationSearchItem>> = _searchResults

    private val _recentSearches = MutableLiveData<List<LocationSearchItem>>()
    val recentSearches: LiveData<List<LocationSearchItem>> = _recentSearches

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var searchJob: Job? = null

    // Mock data
    private val mockLocations = listOf(
        LocationSearchItem("1", "HUE (VVPB)", 16.4018, 107.7022, 40.0, 5, LocationType.WEATHER_STATION),
        LocationSearchItem("2", "Danang", 16.0544, 108.2022, 5.0, 5, LocationType.WEATHER_STATION),
        LocationSearchItem("3", "Vietnam - Non Nuoc", 16.0061, 108.2635, 1.8, 105, LocationType.BEACH, ActivityType.SWIMMING),
        LocationSearchItem("4", "ゴルフ", 16.0478, 108.2208, 1.3, 2, LocationType.LOCATION, ActivityType.GOLF),
        LocationSearchItem("5", "DA_NANG (VVDN)", 16.0544, 108.2022, 5.0, locationType = LocationType.COORDINATES),
        LocationSearchItem("6", "DA_NANG (VVDN)", 16.0544, 108.2022, 5.0, 20, LocationType.WEATHER_STATION),
        LocationSearchItem("7", "16.6558, 16.6558", 16.6558, 16.6558, 64.0, locationType = LocationType.COORDINATES),
        LocationSearchItem("8", "Vietnam - China Beach", 16.0822, 108.2462, 4.0, 173, LocationType.BEACH, ActivityType.SWIMMING),
        LocationSearchItem("9", "My Khe Beach", 16.0638, 108.2490, 5.0, 509, LocationType.BEACH, ActivityType.SWIMMING)
    )

    private val recentSearchesList = mutableListOf<LocationSearchItem>()

    fun searchLocations(query: String) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(300)
            val results = performSearch(query)
            _searchResults.value = results
        }
    }

    private fun performSearch(query: String): List<LocationSearchItem> {
        return mockLocations.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun loadRecentSearches() {
        _recentSearches.value = mockLocations.take(3)
    }

    fun addToRecentSearches(location: LocationSearchItem) {
        recentSearchesList.removeIf { it.id == location.id }
        recentSearchesList.add(0, location)
        if (recentSearchesList.size > 10) recentSearchesList.removeAt(recentSearchesList.size - 1)
        _recentSearches.value = recentSearchesList.toList()
    }

    fun clearRecentSearches() {
        recentSearchesList.clear()
        _recentSearches.value = emptyList()
    }
}
