package com.example.senianmusic.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declara el DataStore a nivel de archivo para que sea un singleton
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token") // Para sesión persistente
        val KEY_USERNAME = stringPreferencesKey("username")
    }

    val serverUrlFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SERVER_URL]
        }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { settings ->
            settings[KEY_SERVER_URL] = url
        }
    }

    // Aquí añadirías funciones para guardar/leer el token y el usuario
}