package com.example.senianmusic.ui.search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.senianmusic.data.local.AppDatabase
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.repository.MusicRepository

class SearchViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            // Creamos todas las dependencias necesarias aquí
            val repository = MusicRepository(
                application,
                AppDatabase.getDatabase(application).songDao(),
                SettingsRepository(application),
                RetrofitClient.getApiService()
            )
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}