package com.example.senianmusic.ui.model

import com.example.senianmusic.data.remote.model.Song

// Clase sellada para representar cada fila en la pantalla de inicio
sealed class HomeScreenRow(val id: Long, val title: String) {
    // Fila de canciones aleatorias
    data class RandomSongsRow(val songs: List<Song>) : HomeScreenRow(1L, "Random")

    // Fila de canciones agregadas recientemente a la biblioteca
    data class RecentlyAddedRow(val songs: List<Song>) : HomeScreenRow(2L, "Agregadas Recientemente")

    // --- CAMBIO DE NOMBRE Y TÍTULO AQUÍ ---
    // Fila de canciones reproducidas recientemente
    data class RecentlyPlayedRow(val songs: List<Song>) : HomeScreenRow(3L, "Reproducidas Recientemente")

    // Fila de canciones recomendadas (top songs)
    data class RecommendedRow(val songs: List<Song>) : HomeScreenRow(4L, "Recomendados")
}