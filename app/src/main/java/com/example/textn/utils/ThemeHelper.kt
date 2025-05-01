package com.example.textn.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.example.textn.R
import com.example.textn.viewmodel.SettingsViewModel

class ThemeHelper {

    fun setUpThemeColors(context: Context) {
        val isDarkTheme = isDarkThemeEnabled(context)
        if (isDarkTheme) {
            context.setTheme(R.style.AppTheme_Dark)
        } else {
            context.setTheme(R.style.AppTheme_Light)
        }
    }

    fun isDarkThemeEnabled(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    // Call this from activities to observe theme changes
    fun setupThemeObserver(activity: AppCompatActivity, viewModel: SettingsViewModel) {
        viewModel.isDarkTheme.observe(activity) { isDarkTheme ->
            ThemeManager.applyTheme(isDarkTheme)
            // Activity will be recreated when theme changes
        }
    }
}
