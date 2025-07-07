package com.example.senianmusic.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.senianmusic.data.local.AppDatabase
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient // <-- IMPORT NECESARIO
import com.example.senianmusic.data.repository.MusicRepository

// La fábrica sigue necesitando solo el Context para empezar.
class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Comprueba si el ViewModel que se pide es MainViewModel.
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {

            // --- LÓGICA DE INYECCIÓN DE DEPENDENCIAS (MANUAL) ---

            // 1. Obtenemos la instancia del servicio de la API desde nuestro RetrofitClient.
            //    Esto funcionará porque RetrofitClient ya fue inicializado en LoginActivity.
            val apiService = RetrofitClient.getApiService()

            // 2. Creamos las dependencias locales como antes.
            val settingsRepository = SettingsRepository(context.applicationContext)
            val songDao = AppDatabase.getDatabase(context.applicationContext).songDao()

            // 3. Creamos la instancia del MusicRepository pasándole TODAS sus dependencias.
            val musicRepository = MusicRepository(
                context = context.applicationContext,
                songDao = songDao,
                settingsRepository = settingsRepository,
                apiService = apiService
            )

            // 4. Creamos el MainViewModel con el repositorio ya listo.
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(musicRepository) as T
        }
        // Si se intenta crear un ViewModel desconocido, lanzamos una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}