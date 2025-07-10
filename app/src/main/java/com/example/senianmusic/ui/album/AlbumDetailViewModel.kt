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

            // --- ¡AQUÍ ESTÁ LA LÓGICA CLAVE! ---
            // Usamos .distinct() para eliminar cualquier canción que esté duplicada en la lista.
            // Kotlin usará el método `equals` de la data class `Song` para comparar.
            // Si dos objetos Song tienen exactamente las mismas propiedades, se considerará un duplicado.
            val uniqueSongs = albumSongs.distinct()

            _songs.value = uniqueSongs
        }
    }

    // El resto de las funciones no necesitan cambios
    suspend fun getStreamUrlForSong(song: Song): String? {
        return repository.getStreamUrlForSong(song)
    }
}