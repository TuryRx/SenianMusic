package com.example.senianmusic.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senianmusic.data.remote.model.SearchResult3
import com.example.senianmusic.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val musicRepository: MusicRepository) : ViewModel() {

    // El StateFlow ahora emitirá un objeto SearchResult3, que puede ser nulo
    private val _searchResults = MutableStateFlow<SearchResult3?>(null)
    val searchResults = _searchResults.asStateFlow()

    // Variable para controlar el debounce
    private var searchJob: Job? = null

    // --- FUNCIÓN MODIFICADA PARA BÚSQUEDA EN TIEMPO REAL ---
    fun executeSearch(query: String) {
        // Cancelamos el trabajo anterior para no hacer búsquedas innecesarias
        searchJob?.cancel()

        if (query.length < 3) { // No buscamos si el texto es muy corto
            _searchResults.value = null // Limpiamos resultados
            return
        }

        // Creamos un nuevo trabajo (Job) con un retraso
        searchJob = viewModelScope.launch {
            delay(500) // Esperamos 500ms antes de lanzar la búsqueda
            val results = musicRepository.search(query) // Asumimos que MusicRepository tiene un método search()
            _searchResults.value = results
        }
    }
}