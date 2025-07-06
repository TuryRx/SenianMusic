package com.example.senianmusic.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.remote.model.Song // Asume que tienes este modelo
import com.example.senianmusic.data.repository.MusicRepository
import kotlinx.coroutines.launch

// 1. El ViewModel recibe el MusicRepository en su constructor
class MainViewModel(private val musicRepository: MusicRepository) : ViewModel() {

    // Un StateFlow para exponer la lista de canciones a la UI
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            // Aquí llamarías a tu repositorio para obtener los datos
            // Por ahora, vamos a usar datos de prueba
            _songs.value = listOf(
                Song("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 355),
                Song("2", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", 482)
            )
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