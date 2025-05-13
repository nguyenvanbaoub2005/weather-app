package com.example.textn.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.textn.viewmodel.SettingsViewModel

class PreferencesRepository(private val context: Context? = null) {

    companion object {
        private const val PREFS_NAME = "com.example.textn.PREFERENCES"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_WEATHER_WIDGET_UNIT = "weather_widget_unit"
        private const val KEY_FAVORITE_PLACES_UNIT = "favorite_places_unit"
        private const val KEY_ROSE_DIAGRAM_MODE = "rose_diagram_mode"
        private const val KEY_WIND_SPEED_UNIT = "wind_speed_unit"
        private const val KEY_CURRENT_SPEED_UNIT = "current_speed_unit"
        private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
        private const val KEY_PRESSURE_UNIT = "pressure_unit"
        private const val KEY_TIDE_UNIT = "tide_unit"
        private const val KEY_DISTANCE_UNIT = "distance_unit"
        private const val KEY_HEIGHT_UNIT = "height_unit"
        private const val KEY_PRECIPITATION_UNIT = "precipitation_unit"
        private const val KEY_TIME_FORMAT = "time_format"
        private const val KEY_FORECAST_PERIOD = "forecast_period"
        private const val KEY_USER_WEIGHT = "user_weight"

        private const val DEFAULT_FONT_SIZE = 16
        private const val DEFAULT_USER_WEIGHT = 154
    }

    private val sharedPreferences: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Theme preferences
    suspend fun getDarkThemeEnabled(): Boolean {
        return sharedPreferences?.getBoolean(KEY_DARK_THEME, true) ?: true
    }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences?.edit()?.putBoolean(KEY_DARK_THEME, enabled)?.apply()
    }

    // Font size preferences
    suspend fun getFontSize(): Int {
        return sharedPreferences?.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE) ?: DEFAULT_FONT_SIZE
    }

    suspend fun setFontSize(size: Int) {
        sharedPreferences?.edit()?.putInt(KEY_FONT_SIZE, size)?.apply()
    }

    // Weather widget unit preferences
    suspend fun getWeatherWidgetUnit(): String? {
        return sharedPreferences?.getString(KEY_WEATHER_WIDGET_UNIT, SettingsViewModel.UNIT_WAVE)
    }

    suspend fun setWeatherWidgetUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_WEATHER_WIDGET_UNIT, unit)?.apply()
    }

    // Favorite places unit preferences
    suspend fun getFavoritePlacesUnit(): String? {
        return sharedPreferences?.getString(KEY_FAVORITE_PLACES_UNIT, SettingsViewModel.UNIT_WAVE)
    }

    suspend fun setFavoritePlacesUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_FAVORITE_PLACES_UNIT, unit)?.apply()
    }

    // Rose diagram mode preferences
    suspend fun getRoseDiagramMode(): String? {
        return sharedPreferences?.getString(KEY_ROSE_DIAGRAM_MODE, SettingsViewModel.MODE_SATELLITE)
    }

    suspend fun setRoseDiagramMode(mode: String) {
        sharedPreferences?.edit()?.putString(KEY_ROSE_DIAGRAM_MODE, mode)?.apply()
    }

    // Wind speed unit preferences
    suspend fun getWindSpeedUnit(): String? {
        return sharedPreferences?.getString(KEY_WIND_SPEED_UNIT, SettingsViewModel.SPEED_KNOTS)
    }

    suspend fun setWindSpeedUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_WIND_SPEED_UNIT, unit)?.apply()
    }

    // Current speed unit preferences
    suspend fun getCurrentSpeedUnit(): String? {
        return sharedPreferences?.getString(KEY_CURRENT_SPEED_UNIT, SettingsViewModel.SPEED_KPH)
    }

    suspend fun setCurrentSpeedUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_CURRENT_SPEED_UNIT, unit)?.apply()
    }

    // Temperature unit preferences
    suspend fun getTemperatureUnit(): String? {
        return sharedPreferences?.getString(KEY_TEMPERATURE_UNIT, SettingsViewModel.TEMP_FAHRENHEIT)
    }

    suspend fun setTemperatureUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_TEMPERATURE_UNIT, unit)?.apply()
    }

    // Pressure unit preferences
    suspend fun getPressureUnit(): String? {
        return sharedPreferences?.getString(KEY_PRESSURE_UNIT, SettingsViewModel.PRESSURE_INHG)
    }

    suspend fun setPressureUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_PRESSURE_UNIT, unit)?.apply()
    }

    // Tide level unit preferences
    suspend fun getTideUnit(): String? {
        return sharedPreferences?.getString(KEY_TIDE_UNIT, SettingsViewModel.TIDE_MLLW)
    }

    suspend fun setTideUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_TIDE_UNIT, unit)?.apply()
    }

    // Distance unit preferences
    suspend fun getDistanceUnit(): String? {
        return sharedPreferences?.getString(KEY_DISTANCE_UNIT, SettingsViewModel.DISTANCE_MILES)
    }

    suspend fun setDistanceUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_DISTANCE_UNIT, unit)?.apply()
    }

    // Height unit preferences
    suspend fun getHeightUnit(): String? {
        return sharedPreferences?.getString(KEY_HEIGHT_UNIT, SettingsViewModel.HEIGHT_FEET)
    }

    suspend fun setHeightUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_HEIGHT_UNIT, unit)?.apply()
    }

    // Precipitation unit preferences
    suspend fun getPrecipitationUnit(): String? {
        return sharedPreferences?.getString(KEY_PRECIPITATION_UNIT, SettingsViewModel.PRECIP_INCH)
    }

    suspend fun setPrecipitationUnit(unit: String) {
        sharedPreferences?.edit()?.putString(KEY_PRECIPITATION_UNIT, unit)?.apply()
    }

    // Time format preferences
    suspend fun getTimeFormat(): String? {
        return sharedPreferences?.getString(KEY_TIME_FORMAT, SettingsViewModel.TIME_24H)
    }

    suspend fun setTimeFormat(format: String) {
        sharedPreferences?.edit()?.putString(KEY_TIME_FORMAT, format)?.apply()
    }

    // Forecast period preferences
    suspend fun getForecastPeriod(): String? {
        return sharedPreferences?.getString(KEY_FORECAST_PERIOD, SettingsViewModel.FORECAST_3H)
    }

    suspend fun setForecastPeriod(period: String) {
        sharedPreferences?.edit()?.putString(KEY_FORECAST_PERIOD, period)?.apply()
    }

    // User weight preferences
    suspend fun getUserWeight(): Int {
        return sharedPreferences?.getInt(KEY_USER_WEIGHT, DEFAULT_USER_WEIGHT) ?: DEFAULT_USER_WEIGHT
    }

    suspend fun setUserWeight(weight: Int) {
        sharedPreferences?.edit()?.putInt(KEY_USER_WEIGHT, weight)?.apply()
    }
}