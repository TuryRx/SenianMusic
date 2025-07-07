package com.example.senianmusic.data.repository

import android.content.Context
import android.util.Log
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.local.dao.SongDao
import com.example.senianmusic.data.remote.NavidromeApiService
import com.example.senianmusic.data.remote.model.Artist
import com.example.senianmusic.data.remote.model.Song
import kotlinx.coroutines.flow.first

class MusicRepository(
    val context: Context, // <-- Ahora es público
    private val songDao: SongDao, // songDao puede seguir siendo privado
    val settingsRepository: SettingsRepository, // <-- Ahora es público
    private val apiService: NavidromeApiService // apiService puede seguir siendo privado
) {

    suspend fun fetchArtists(): List<Artist> {
        // ... (esta función ya está bien) ...
        return try {
            val user = settingsRepository.usernameFlow.first() ?: return emptyList()
            val token = settingsRepository.tokenFlow.first() ?: return emptyList()
            val salt = settingsRepository.saltFlow.first() ?: return emptyList()
            val response = apiService.getArtists(user, token, salt)
            if (response.isSuccessful) {
                response.body()?.subsonicResponse?.artists?.index?.flatMap { it.artist } ?: emptyList()
            } else {
                Log.e("MusicRepository", "Error al obtener artistas: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción al obtener artistas", e)
            emptyList()
        }
    }

    suspend fun fetchRandomSongs(): List<Song> {
        return try {
            val user = settingsRepository.usernameFlow.first() ?: return emptyList()
            val token = settingsRepository.tokenFlow.first() ?: return emptyList()
            val salt = settingsRepository.saltFlow.first() ?: return emptyList()
            val baseUrl = settingsRepository.serverUrlFlow.first() ?: return emptyList()

            Log.d("MusicRepository", "Pidiendo 50 canciones aleatorias a la API...")
            val response = apiService.getRandomSongs(user, token, salt, size = 50)

            if (response.isSuccessful) {
                val songsFromApi = response.body()?.subsonicResponse?.randomSongs?.songList ?: emptyList()
                Log.d("MusicRepository", "Recibidas ${songsFromApi.size} canciones de la API.")

                // --- LÓGICA CORREGIDA ---
                // Mapeamos la respuesta para construir la URL y DEVOLVEMOS explícitamente el resultado.
                return songsFromApi.map { song ->
                    song.apply {
                        coverArtUrl = song.buildCoverArtUrl(baseUrl, user, token, salt)
                    }
                }
            } else {
                Log.e("MusicRepository", "Error al obtener canciones aleatorias: ${response.code()}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción al obtener canciones aleatorias", e)
            return emptyList()
        }
    }

    fun startSongDownload(song: Song) {
        Log.d("MusicRepository", "Solicitando descarga para la canción: ${song.title}")
        // TODO: Implementar lógica de descarga.
    }
}