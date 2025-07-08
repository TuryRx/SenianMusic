package com.example.senianmusic.ui.model

import com.example.senianmusic.data.remote.model.Album // ¡Importa Album!
import com.example.senianmusic.data.remote.model.Song

sealed class HomeScreenRow(val id: Long, val title: String) {
    data class RandomSongsRow(val songs: List<Song>) : HomeScreenRow(1L, "Random")

    // --- CAMBIO: AHORA CONTIENE ÁLBUMES ---
    data class RecentlyAddedRow(val albums: List<Album>) : HomeScreenRow(2L, "Agregadas Recientemente")
    data class RecentlyPlayedRow(val albums: List<Album>) : HomeScreenRow(3L, "Reproducidas Recientemente")

    data class RecommendedRow(val songs: List<Song>) : HomeScreenRow(4L, "Recomendados")
}