package com.example.textn.data.model


data class DayForecastItem(
    val id: Long,
    val day: String,          // Thứ (Mon, Tue...)
    val date: String,         // dd/MM
    val temperature: String,  // 27°C
    val description: String,  // Trời nắng
    val iconId: Int,          // R.drawable.ic_XXX
    val humidity: String,     // 80%
    val windSpeed: String     // 5.2 m/s
)
