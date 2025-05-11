package com.example.textn.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CommunityViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            return CommunityViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
