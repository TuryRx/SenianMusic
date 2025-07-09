package com.example.senianmusic.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.senianmusic.data.local.AppDatabase
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.repository.MusicRepository

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {

            // Creamos las dependencias como ya lo hacías
            val apiService = RetrofitClient.getApiService()
            val settingsRepository = SettingsRepository(context.applicationContext)
            val songDao = AppDatabase.getDatabase(context.applicationContext).songDao()
            val musicRepository = MusicRepository(
                context = context.applicationContext,
                songDao = songDao,
                settingsRepository = settingsRepository,
                apiService = apiService
            )

            // --- CAMBIO 2: Pasamos AMBOS repositorios al constructor del ViewModel ---
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(musicRepository, settingsRepository) as T // <-- Línea modificada
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}