package com.example.senianmusic.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.local.AppDatabase
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import com.example.senianmusic.ui.model.HomeScreenRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- CAMBIO 1: El constructor ahora acepta y guarda ambos repositorios ---
class MainViewModel(
    private val musicRepository: MusicRepository,
    val settingsRepository: SettingsRepository // <-- Añadido aquí
) : ViewModel() {

    private val _homeScreenRows = MutableStateFlow<List<HomeScreenRow>>(emptyList())
    val homeScreenRows = _homeScreenRows.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            val randomSongsDeferred = async { musicRepository.fetchRandomSongs() }
            val newestAlbumsDeferred = async { musicRepository.fetchAlbumList("newest") }
            val recentAlbumsDeferred = async { musicRepository.fetchAlbumList("recent") }
            val recommendedSongsDeferred = async { musicRepository.fetchTopSongs() }

            val randomSongs = randomSongsDeferred.await()
            val newestAlbums = newestAlbumsDeferred.await()
            val recentAlbums = recentAlbumsDeferred.await()
            val recommendedSongs = recommendedSongsDeferred.await()

            val rows = mutableListOf<HomeScreenRow>()
            if (randomSongs.isNotEmpty()) rows.add(HomeScreenRow.RandomSongsRow(randomSongs))
            if (newestAlbums.isNotEmpty()) rows.add(HomeScreenRow.RecentlyAddedRow(newestAlbums))
            if (recentAlbums.isNotEmpty()) rows.add(HomeScreenRow.RecentlyPlayedRow(recentAlbums))
            if (recommendedSongs.isNotEmpty()) rows.add(HomeScreenRow.RecommendedRow(recommendedSongs))

            _homeScreenRows.value = rows
        }
    }

    suspend fun getStreamUrlForSong(song: Song): String? {
        return musicRepository.getStreamUrlForSong(song)
    }

    suspend fun getCoverUrlForSong(song: Song): String? {
        return musicRepository.getCoverUrlForSong(song)
    }

    suspend fun fetchAlbumSongs(albumId: String): List<Song> {
        return musicRepository.fetchAlbumDetails(albumId)
    }

    fun scrobbleSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.scrobbleSong(song.id)
        }
    }

    // --- ¡NUEVA FUNCIÓN DE LOGOUT! ---
    // Esta función ahora compilará sin problemas porque `settingsRepository`
    // es una propiedad de la clase.
    fun logout() {
        viewModelScope.launch {
            settingsRepository.clearLoginSession()
            Log.d("MainViewModel", "Sesión de usuario eliminada de DataStore.")
        }
    }
}