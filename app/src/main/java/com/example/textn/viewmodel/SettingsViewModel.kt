package com.example.textn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textn.repository.PreferencesRepository
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    // Preferences repository - would be injected in a real app
    private val preferencesRepository = PreferencesRepository()

    // Theme preference
    private val _isDarkTheme = MutableLiveData<Boolean>()
    val isDarkTheme: LiveData<Boolean> = _isDarkTheme

    // Font size preference
    private val _fontSize = MutableLiveData<Int>()
    val fontSize: LiveData<Int> = _fontSize

    // Unit preferences - existing
    private val _weatherWidgetUnit = MutableLiveData<String>()
    val weatherWidgetUnit: LiveData<String> = _weatherWidgetUnit

    private val _favoritePlacesUnit = MutableLiveData<String>()
    val favoritePlacesUnit: LiveData<String> = _favoritePlacesUnit

    private val _roseDiagramMode = MutableLiveData<String>()
    val roseDiagramMode: LiveData<String> = _roseDiagramMode

    private val _windSpeedUnit = MutableLiveData<String>()
    val windSpeedUnit: LiveData<String> = _windSpeedUnit

    private val _currentSpeedUnit = MutableLiveData<String>()
    val currentSpeedUnit: LiveData<String> = _currentSpeedUnit

    // Unit preferences - new from XML
    private val _temperatureUnit = MutableLiveData<String>()
    val temperatureUnit: LiveData<String> = _temperatureUnit

    private val _pressureUnit = MutableLiveData<String>()
    val pressureUnit: LiveData<String> = _pressureUnit

    private val _tideUnit = MutableLiveData<String>()
    val tideUnit: LiveData<String> = _tideUnit

    private val _distanceUnit = MutableLiveData<String>()
    val distanceUnit: LiveData<String> = _distanceUnit

    private val _heightUnit = MutableLiveData<String>()
    val heightUnit: LiveData<String> = _heightUnit

    private val _precipitationUnit = MutableLiveData<String>()
    val precipitationUnit: LiveData<String> = _precipitationUnit

    private val _timeFormat = MutableLiveData<String>()
    val timeFormat: LiveData<String> = _timeFormat

    private val _forecastPeriod = MutableLiveData<String>()
    val forecastPeriod: LiveData<String> = _forecastPeriod

    private val _userWeight = MutableLiveData<Int>()
    val userWeight: LiveData<Int> = _userWeight

    companion object {
        // Constants for unit types - existing
        const val UNIT_OFF = "off"
        const val UNIT_WIND = "wind"
        const val UNIT_WAVE = "wave"
        const val UNIT_TEMP = "temperature"
        const val UNIT_SNOW = "snow"

        // Constants for rose diagram modes
        const val MODE_COMPASS = "compass"
        const val MODE_SATELLITE = "satellite"

        // Constants for speed units
        const val SPEED_MPS = "mps"
        const val SPEED_MPH = "mph"
        const val SPEED_KPH = "kph"
        const val SPEED_KNOTS = "knots"

        // Constants for temperature units
        const val TEMP_CELSIUS = "celsius"
        const val TEMP_FAHRENHEIT = "fahrenheit"
        const val TEMP_KELVIN = "kelvin"

        // Constants for pressure units
        const val PRESSURE_HPA = "hpa"
        const val PRESSURE_INHG = "inhg"
        const val PRESSURE_MMHG = "mmhg"

        // Constants for tide level units
        const val TIDE_LAT = "lat"
        const val TIDE_MLLW = "mllw"
        const val TIDE_MSL = "msl"

        // Constants for distance units
        const val DISTANCE_KM = "kilometers"
        const val DISTANCE_MILES = "miles"
        const val DISTANCE_NAUTICAL = "nautical_miles"

        // Constants for height units
        const val HEIGHT_METERS = "meters"
        const val HEIGHT_FEET = "feet"

        // Constants for precipitation units
        const val PRECIP_MM = "mm"
        const val PRECIP_INCH = "inch"

        // Constants for time format
        const val TIME_24H = "24h"
        const val TIME_12H = "12h"

        // Constants for forecast period
        const val FORECAST_1H = "1h"
        const val FORECAST_3H = "3h"
    }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load existing settings
            _isDarkTheme.value = preferencesRepository.getDarkThemeEnabled()
            _fontSize.value = preferencesRepository.getFontSize()
            _weatherWidgetUnit.value = preferencesRepository.getWeatherWidgetUnit() ?: UNIT_WAVE
            _favoritePlacesUnit.value = preferencesRepository.getFavoritePlacesUnit() ?: UNIT_WAVE
            _roseDiagramMode.value = preferencesRepository.getRoseDiagramMode() ?: MODE_SATELLITE
            _windSpeedUnit.value = preferencesRepository.getWindSpeedUnit() ?: SPEED_KNOTS
            _currentSpeedUnit.value = preferencesRepository.getCurrentSpeedUnit() ?: SPEED_KPH

            // Load new settings
            _temperatureUnit.value = preferencesRepository.getTemperatureUnit() ?: TEMP_FAHRENHEIT
            _pressureUnit.value = preferencesRepository.getPressureUnit() ?: PRESSURE_INHG
            _tideUnit.value = preferencesRepository.getTideUnit() ?: TIDE_MLLW
            _distanceUnit.value = preferencesRepository.getDistanceUnit() ?: DISTANCE_MILES
            _heightUnit.value = preferencesRepository.getHeightUnit() ?: HEIGHT_FEET
            _precipitationUnit.value = preferencesRepository.getPrecipitationUnit() ?: PRECIP_INCH
            _timeFormat.value = preferencesRepository.getTimeFormat() ?: TIME_24H
            _forecastPeriod.value = preferencesRepository.getForecastPeriod() ?: FORECAST_3H
            _userWeight.value = preferencesRepository.getUserWeight()
        }
    }

    // Existing functions
    fun toggleTheme() {
        val newThemeValue = !(isDarkTheme.value ?: true)
        _isDarkTheme.value = newThemeValue
        viewModelScope.launch {
            preferencesRepository.setDarkThemeEnabled(newThemeValue)
        }
    }

    fun setFontSize(size: Int) {
        _fontSize.value = size
        viewModelScope.launch {
            preferencesRepository.setFontSize(size)
        }
    }

    fun setWeatherWidgetUnit(unit: String) {
        _weatherWidgetUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setWeatherWidgetUnit(unit)
        }
    }

    fun setFavoritePlacesUnit(unit: String) {
        _favoritePlacesUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setFavoritePlacesUnit(unit)
        }
    }

    fun setRoseDiagramMode(mode: String) {
        _roseDiagramMode.value = mode
        viewModelScope.launch {
            preferencesRepository.setRoseDiagramMode(mode)
        }
    }

    fun setWindSpeedUnit(unit: String) {
        _windSpeedUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setWindSpeedUnit(unit)
        }
    }

    fun setCurrentSpeedUnit(unit: String) {
        _currentSpeedUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setCurrentSpeedUnit(unit)
        }
    }

    // New functions for XML units
    fun setTemperatureUnit(unit: String) {
        _temperatureUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setTemperatureUnit(unit)
        }
    }

    fun setPressureUnit(unit: String) {
        _pressureUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setPressureUnit(unit)
        }
    }

    fun setTideUnit(unit: String) {
        _tideUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setTideUnit(unit)
        }
    }

    fun setDistanceUnit(unit: String) {
        _distanceUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setDistanceUnit(unit)
        }
    }

    fun setHeightUnit(unit: String) {
        _heightUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setHeightUnit(unit)
        }
    }

    fun setPrecipitationUnit(unit: String) {
        _precipitationUnit.value = unit
        viewModelScope.launch {
            preferencesRepository.setPrecipitationUnit(unit)
        }
    }

    fun setTimeFormat(format: String) {
        _timeFormat.value = format
        viewModelScope.launch {
            preferencesRepository.setTimeFormat(format)
        }
    }

    fun setForecastPeriod(period: String) {
        _forecastPeriod.value = period
        viewModelScope.launch {
            preferencesRepository.setForecastPeriod(period)
        }
    }

    fun setUserWeight(weight: Int) {
        _userWeight.value = weight
        viewModelScope.launch {
            preferencesRepository.setUserWeight(weight)
        }
    }

    fun sendFeedback(email: String, message: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Logic to send feedback email
            onComplete(true)
        }
    }
}