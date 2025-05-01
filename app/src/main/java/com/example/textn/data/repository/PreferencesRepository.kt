package com.example.textn.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesRepository(private val context: Context? = null) {

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_FONT_SIZE = "font_size"
        private const val DEFAULT_FONT_SIZE = 16
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In a real app, this would use DataStore or other modern storage

    suspend fun getDarkThemeEnabled(): Boolean {
        return prefs?.getBoolean(KEY_DARK_THEME, true) ?: true
    }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        prefs?.edit {
            putBoolean(KEY_DARK_THEME, enabled)
        }
    }

    suspend fun getFontSize(): Int {
        return prefs?.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE) ?: DEFAULT_FONT_SIZE
    }

    suspend fun setFontSize(size: Int) {
        prefs?.edit {
            putInt(KEY_FONT_SIZE, size)
        }
    }
}