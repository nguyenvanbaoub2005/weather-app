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

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _isDarkTheme.value = preferencesRepository.getDarkThemeEnabled()
            _fontSize.value = preferencesRepository.getFontSize()
        }
    }

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

    fun sendFeedback(email: String, message: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Logic to send feedback email
            onComplete(true)
        }
    }
}