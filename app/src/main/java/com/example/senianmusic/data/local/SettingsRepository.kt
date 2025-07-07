package com.example.senianmusic.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Declaramos la instancia de DataStore aquí, a nivel de archivo.
//    Esto asegura que solo haya UNA instancia en toda la app.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) { // <-- Seguimos necesitando el contexto

    // 2. Definimos las claves para nuestras preferencias.
    companion object {
        val SERVER_URL_KEY = stringPreferencesKey("server_url")
        // Aquí puedes añadir más claves, como USERNAME_KEY, TOKEN_KEY, etc.
    }

    // 3. Creamos un Flow para leer la URL del servidor.
    val serverUrlFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SERVER_URL_KEY]
        }

    // 4. Creamos una función suspendida para guardar la URL del servidor.
    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { settings ->
            settings[SERVER_URL_KEY] = url
        }
    }

    // Puedes añadir más funciones para guardar/leer otras preferencias aquí
}