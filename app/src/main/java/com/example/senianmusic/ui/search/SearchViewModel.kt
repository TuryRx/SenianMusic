package com.example.senianmusic.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val musicRepository: MusicRepository) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun executeSearch(query: String) {
        viewModelScope.launch {
            val results = musicRepository.searchSongs(query)
            _searchResults.value = results
        }
    }

    suspend fun getStreamUrlForSong(song: Song): String? {
        return musicRepository.getStreamUrlForSong(song)
    }

}