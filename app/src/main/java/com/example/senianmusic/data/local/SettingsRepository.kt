package com.example.senianmusic.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// La instancia única de DataStore para toda la app.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        // Claves para todas las preferencias que vamos a guardar.
        val SERVER_URL_KEY = stringPreferencesKey("server_url")
        val USERNAME_KEY = stringPreferencesKey("username")
        val TOKEN_KEY = stringPreferencesKey("token") // El token MD5
        val SALT_KEY = stringPreferencesKey("salt")
    }

    // Flujos para leer los datos de la sesión guardada.
    val serverUrlFlow: Flow<String?> = context.dataStore.data.map { it[SERVER_URL_KEY] }
    val usernameFlow: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }
    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val saltFlow: Flow<String?> = context.dataStore.data.map { it[SALT_KEY] }

    // Función para guardar toda la sesión de una vez.
    suspend fun saveLoginSession(url: String, user: String, token: String, salt: String) {
        context.dataStore.edit { settings ->
            settings[SERVER_URL_KEY] = url
            settings[USERNAME_KEY] = user
            settings[TOKEN_KEY] = token
            settings[SALT_KEY] = salt
        }
    }

    // Función para borrar la sesión (logout).
    suspend fun clearLoginSession() {
        context.dataStore.edit { it.clear() }
    }
}