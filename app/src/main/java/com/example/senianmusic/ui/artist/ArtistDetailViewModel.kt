package com.example.senianmusic.ui.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtistDetailViewModel(private val repository: MusicRepository) : ViewModel() {

    // Usaremos esta etiqueta para filtrar los logs y ver solo lo que nos interesa.
    private val TAG = "ArtistDetailVM"

    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadArtistData(artistId: String) {
        // Log para saber que la función se ha iniciado correctamente.
        Log.d(TAG, "Iniciando carga de datos para el artista con ID: $artistId")

        viewModelScope.launch {
            // Ponemos la UI en estado de carga.
            _uiState.update { it.copy(isLoading = true) }

            // --- PASO 1: OBTENER LOS DETALLES DEL ARTISTA (INCLUYE SUS ÁLBUMES) ---
            val artistDetails = repository.fetchArtistDetails(artistId)

            // Si no se encuentran detalles, es un error grave. Salimos.
            if (artistDetails == null) {
                Log.e(TAG, "ERROR: No se pudieron obtener los detalles para el artista ID: $artistId. Abortando.")
                _uiState.update { it.copy(isLoading = false, artistName = "Artista no encontrado") }
                return@launch
            }
            Log.d(TAG, "Detalles obtenidos para '${artistDetails.name}'. Encontrados ${artistDetails.albumList.size} álbumes.")

            // --- PASO 2: ENCONTRAR LA IMAGEN DEL ARTISTA (LA LÓGICA CLAVE) ---
            // El repositorio ya se encargó de poner la URL completa en `coverArtUrl` de cada álbum.
            // Buscamos la primera URL de imagen que no sea nula o vacía en la lista de álbumes.
            var artistImageUrl = artistDetails.albumList.firstNotNullOfOrNull { it.coverArtUrl }

            Log.d(TAG, "Imagen encontrada en los álbumes: $artistImageUrl")


            // --- PASO 3: OBTENER LAS CANCIONES MÁS POPULARES ---
            // Las obtenemos por separado para mostrarlas en la lista de "Canciones".
            val topSongs = repository.fetchAllSongsByArtist(artistId)
            Log.d(TAG, "Se encontraron ${topSongs.size} canciones en total para el artista.")

            // --- PASO 4: FALLBACK PARA LA IMAGEN ---
            // Si después de ver todos los álbumes NO encontramos una imagen,
            // buscamos en la lista de canciones que acabamos de obtener.
            if (artistImageUrl == null) {
                Log.w(TAG, "No se encontró imagen en los álbumes. Buscando en las canciones como plan B.")
                artistImageUrl = topSongs.firstNotNullOfOrNull { it.coverArtUrl }
                Log.d(TAG, "Imagen encontrada en las canciones: $artistImageUrl")
            }

            // --- PASO 5: ACTUALIZAR EL ESTADO FINAL DE LA UI ---
            // Ahora que tenemos toda la información, la enviamos al Fragment para que la dibuje.
            _uiState.update {
                it.copy(
                    isLoading = false,
                    artistName = artistDetails.name,
                    artistImageUrl = artistImageUrl, // La URL final que encontramos.
                    topSongs = topSongs,
                    albums = artistDetails.albumList
                )
            }
            Log.i(TAG, "¡Estado de la UI actualizado! Nombre: ${artistDetails.name}, URL de imagen final: $artistImageUrl")
        }
    }
}