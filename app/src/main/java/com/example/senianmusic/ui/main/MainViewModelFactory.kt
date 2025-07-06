package com.senian.senianmusic.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.senian.senianmusic.data.local.AppDatabase
import com.senian.senianmusic.data.local.SettingsRepository
import com.senian.senianmusic.data.repository.MusicRepository

// 1. La fábrica necesita el Context para poder crear las dependencias.
class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    // 2. Este es el método clave que el sistema llamará.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Comprueba si el ViewModel que se pide es el MainViewModel
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {

            // --- AQUÍ ESTÁ LA LÓGICA DE CREACIÓN DE DEPENDENCIAS ---
            // Esto es exactamente el código que te mostré antes, pero en el lugar correcto.

            // a. Crea las dependencias que MusicRepository necesita
            val db = AppDatabase.getDatabase(context.applicationContext)
            val songDao = db.songDao()
            val settingsRepository = SettingsRepository(context.applicationContext)

            // b. Crea la instancia del MusicRepository con todo lo que necesita
            val musicRepository = MusicRepository(songDao, settingsRepository, context.applicationContext)

            // c. Crea el MainViewModel y se lo pasa al sistema.
            // La supresión de advertencia es segura aquí porque ya hemos comprobado el tipo.
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(musicRepository) as T
        }
        // Si se pide un ViewModel desconocido, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}