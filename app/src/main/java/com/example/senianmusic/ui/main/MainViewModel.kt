package com.example.senianmusic.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import com.example.senianmusic.ui.model.HomeScreenRow
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val musicRepository: MusicRepository) : ViewModel() {

    private val _homeScreenRows = MutableStateFlow<List<HomeScreenRow>>(emptyList())
    val homeScreenRows = _homeScreenRows.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            // ...
            val randomSongsDeferred = async { musicRepository.fetchRandomSongs() }
            // --- CAMBIO: LLAMAMOS A LA NUEVA FUNCIÓN ---
            val newestAlbumsDeferred = async { musicRepository.fetchAlbumList("newest") }
            val recentAlbumsDeferred = async { musicRepository.fetchAlbumList("recent") }
            val recommendedSongsDeferred = async { musicRepository.fetchTopSongs() }

            val randomSongs = randomSongsDeferred.await()
            val newestAlbums = newestAlbumsDeferred.await() // <-- Ahora son álbumes
            val recentAlbums = recentAlbumsDeferred.await() // <-- Ahora son álbumes
            val recommendedSongs = recommendedSongsDeferred.await()

            val rows = mutableListOf<HomeScreenRow>()
            if (randomSongs.isNotEmpty()) rows.add(HomeScreenRow.RandomSongsRow(randomSongs))
            // --- CAMBIO: AÑADIMOS FILAS DE ÁLBUMES ---
            if (newestAlbums.isNotEmpty()) rows.add(HomeScreenRow.RecentlyAddedRow(newestAlbums))
            if (recentAlbums.isNotEmpty()) rows.add(HomeScreenRow.RecentlyPlayedRow(recentAlbums))
            if (recommendedSongs.isNotEmpty()) rows.add(HomeScreenRow.RecommendedRow(recommendedSongs))

            _homeScreenRows.value = rows
            // ...
        }
    }


    // --- ¡CORRECCIÓN IMPORTANTE! ---
    // El ViewModel DELEGA la lógica al Repositorio. No la implementa él mismo.
    // Esto mantiene el código limpio y las responsabilidades separadas.

    /**
     * Obtiene la URL de streaming para una canción.
     * Esta función es llamada por el Fragment para iniciar la reproducción.
     */
    suspend fun getStreamUrlForSong(song: Song): String? {
        return musicRepository.getStreamUrlForSong(song)
    }

    /**
     * Obtiene la URL de la carátula para una canción.
     * Esta función es llamada por la barra inferior (MainActivity) para mostrar la imagen.
     */
    suspend fun getCoverUrlForSong(song: Song): String? {
        return musicRepository.getCoverUrlForSong(song)
    }

    suspend fun fetchAlbumSongs(albumId: String): List<Song> {
        return musicRepository.fetchAlbumDetails(albumId)
    }
}