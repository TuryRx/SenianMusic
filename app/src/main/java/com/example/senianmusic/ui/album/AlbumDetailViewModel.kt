package com.example.senianmusic.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlbumDetailViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    fun loadAlbumSongs(albumId: String) {
        viewModelScope.launch {
            val albumSongs = repository.fetchAlbumDetails(albumId)

            // Filtra duplicados por nombre de canción
            val uniqueSongs = albumSongs.distinctBy { it.title.lowercase() }

            _songs.value = uniqueSongs
        }
    }
}