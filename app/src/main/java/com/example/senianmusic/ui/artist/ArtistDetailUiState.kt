package com.example.senianmusic.ui.artist

import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Song

data class ArtistDetailUiState(
    val isLoading: Boolean = true,
    val artistName: String? = null,
    val artistImageUrl: String? = null,

    // Solo necesitamos las listas maestras, limpias y completas
    val topSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList()
)