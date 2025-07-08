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
            Log.d("MainViewModel", "Iniciando carga de datos de la pantalla de inicio...")
            try {
                // Lanzamos todas las llamadas a la API en paralelo
                val randomSongsDeferred = async { musicRepository.fetchRandomSongs() }

                // --- ¡AQUÍ ESTÁ LA LÓGICA CORREGIDA! ---
                // "newest" para los álbumes recién AÑADIDOS a la biblioteca
                val newestSongsDeferred = async { musicRepository.fetchSongsFromAlbumList("newest") }
                // "recent" para los álbumes recién REPRODUCIDOS
                val recentSongsDeferred = async { musicRepository.fetchSongsFromAlbumList("recent") }

                val recommendedSongsDeferred = async { musicRepository.fetchTopSongs() }

                // Esperamos a que todas las llamadas terminen
                val randomSongs = randomSongsDeferred.await()
                val newestSongs = newestSongsDeferred.await() // <-- Nueva variable
                val recentSongs = recentSongsDeferred.await() // <-- Ahora es para las reproducidas
                val recommendedSongs = recommendedSongsDeferred.await()

                val rows = mutableListOf<HomeScreenRow>()
                if (randomSongs.isNotEmpty()) {
                    rows.add(HomeScreenRow.RandomSongsRow(randomSongs))
                }
                // Usamos la variable y el tipo de fila correctos
                if (newestSongs.isNotEmpty()) {
                    rows.add(HomeScreenRow.RecentlyAddedRow(newestSongs))
                }
                // Usamos la variable y el tipo de fila correctos
                if (recentSongs.isNotEmpty()) {
                    rows.add(HomeScreenRow.RecentlyPlayedRow(recentSongs))
                }
                if (recommendedSongs.isNotEmpty()) {
                    rows.add(HomeScreenRow.RecommendedRow(recommendedSongs))
                }

                _homeScreenRows.value = rows
                Log.d("MainViewModel", "Carga finalizada. ${rows.size} filas creadas.")

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error al cargar los datos de inicio", e)
            }
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
}