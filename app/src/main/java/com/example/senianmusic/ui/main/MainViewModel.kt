package com.example.senianmusic.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
// --- IMPORTS CORREGIDOS ---
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. El ViewModel recibe el MusicRepository en su constructor
class MainViewModel(private val musicRepository: MusicRepository) : ViewModel() {

    // Un StateFlow para exponer la lista de canciones a la UI
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    // En MainViewModel.kt
    fun loadInitialData() {
        viewModelScope.launch {
            // Obtenemos la URL base del servidor desde el repositorio de configuración.
            // Como aún no lo tenemos conectado, usamos una URL falsa para probar.
            val baseUrl = "http://fake.navidrome.server:4533/" // <-- URL de prueba

            // Creas objetos Song como los tienes definidos
            val song1 = Song("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 355, "art-123")
            val song2 = Song("2", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", 482, "art-456")

            // Aquí podrías crear un nuevo objeto o modificar los existentes con la URL completa
            // Pero para ser más limpios, lo haremos en el Presenter

            _songs.value = listOf(song1, song2)
        }
    }

    // Aquí expondrías los datos a la UI, por ejemplo, con StateFlow o LiveData
    // val artists = MutableStateFlow<List<Artist>>(emptyList())
    // ...

    fun onDownloadSongClicked(song: Song) {
        // La UI llama a esta función cuando el usuario pulsa "descargar"
        viewModelScope.launch {
            musicRepository.startSongDownload(song)
        }
    }

    // fun loadArtists() {
    //     viewModelScope.launch {
    //         val artistList = musicRepository.fetchArtists()
    //         artists.value = artistList
    //     }
    // }
}