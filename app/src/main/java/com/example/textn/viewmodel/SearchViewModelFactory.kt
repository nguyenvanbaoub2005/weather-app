package com.example.textn.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.textn.repository.LocationRepository

class SearchViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            val repository = LocationRepository()
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}