package com.example.textn.model

data class LocationSearchItem(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double = 0.0,
    val rating: Int = 0,
    val locationType: LocationType = LocationType.LOCATION,
    val activityType: ActivityType? = null
)

enum class LocationType {
    WEATHER_STATION,
    COORDINATES,
    LOCATION,
    BEACH
}

enum class ActivityType {
    SWIMMING,
    HIKING,
    GOLF,
    NONE
}