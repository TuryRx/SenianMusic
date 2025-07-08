package com.example.senianmusic.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.remote.model.Artist
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import com.example.senianmusic.player.MusicPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(private val musicRepository: MusicRepository) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists = _artists.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            Log.d("MainViewModel", "Iniciando carga de datos iniciales...")
            val songList = musicRepository.fetchRandomSongs()
            _songs.value = songList
            Log.d("MainViewModel", "Carga de datos finalizada. ${songList.size} canciones emitidas a la UI.")
        }
    }

    // Tu función playSong existente se queda como está.
    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                val baseUrl = musicRepository.settingsRepository.serverUrlFlow.first()
                val user = musicRepository.settingsRepository.usernameFlow.first()
                val token = musicRepository.settingsRepository.tokenFlow.first()
                val salt = musicRepository.settingsRepository.saltFlow.first()

                if (baseUrl != null && user != null && token != null && salt != null) {
                    val streamUrl = song.getStreamUrl(baseUrl, user, token, salt)
                    Log.d("MainViewModel", "Reproduciendo URL: $streamUrl")
                    MusicPlayer.play(musicRepository.context, streamUrl)
                } else {
                    Log.e("MainViewModel", "No se pudo reproducir la canción: Faltan datos de sesión.")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error al intentar reproducir la canción", e)
            }
        }
    }

    fun onDownloadSongClicked(song: Song) {
        musicRepository.startSongDownload(song)
    }

    // =====================================================================
    // === ¡AQUÍ ESTÁ LA ÚNICA FUNCIÓN QUE NECESITAS AÑADIR! ===
    // =====================================================================
    /**
     * Toma un objeto Song y construye la URL completa de su carátula.
     * Esta función será llamada desde MainFragment para obtener la imagen
     * de la fila "Ahora Suena".
     */
    suspend fun getCoverUrlForSong(song: Song): String? {
        return try {
            // Accedemos a los datos de la sesión a través del repositorio, como ya haces.
            val baseUrl = musicRepository.settingsRepository.serverUrlFlow.first()
            val user = musicRepository.settingsRepository.usernameFlow.first()
            val token = musicRepository.settingsRepository.tokenFlow.first()
            val salt = musicRepository.settingsRepository.saltFlow.first()

            if (baseUrl != null && user != null && token != null && salt != null) {
                // Llamamos al método del objeto Song para construir la URL.
                song.buildCoverArtUrl(baseUrl, user, token, salt)
            } else {
                null // Si falta algún dato de la sesión, no podemos construir la URL.
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error al obtener la URL de la carátula", e)
            null // En caso de error, devolvemos null para que no se caiga la app.
        }
    }

    suspend fun getStreamUrlForSong(song: Song): String? {
        return try {
            val baseUrl = musicRepository.settingsRepository.serverUrlFlow.first()
            val user = musicRepository.settingsRepository.usernameFlow.first()
            val token = musicRepository.settingsRepository.tokenFlow.first()
            val salt = musicRepository.settingsRepository.saltFlow.first()

            if (baseUrl != null && user != null && token != null && salt != null) {
                song.getStreamUrl(baseUrl, user, token, salt)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}