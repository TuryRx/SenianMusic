package com.example.senianmusic.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.remote.model.Artist
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import com.example.senianmusic.player.MusicPlayer // <-- IMPORTAMOS NUESTRO REPRODUCTOR
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

    // --- FUNCIÓN NUEVA Y MEJORADA ---
    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                // Obtenemos los datos de sesión guardados para construir la URL de streaming
                val baseUrl = musicRepository.settingsRepository.serverUrlFlow.first()
                val user = musicRepository.settingsRepository.usernameFlow.first()
                val token = musicRepository.settingsRepository.tokenFlow.first()
                val salt = musicRepository.settingsRepository.saltFlow.first()

                if (baseUrl != null && user != null && token != null && salt != null) {
                    val streamUrl = song.getStreamUrl(baseUrl, user, token, salt)
                    Log.d("MainViewModel", "Reproduciendo URL: $streamUrl")

                    // Le decimos al reproductor que reproduzca esta URL
                    // Necesitamos el contexto, que lo obtenemos del repositorio
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
}