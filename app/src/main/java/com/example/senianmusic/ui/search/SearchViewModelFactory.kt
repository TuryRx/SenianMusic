package com.example.senianmusic.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.senianmusic.data.repository.MusicRepository

/**
 * Factory para crear instancias de SearchViewModel.
 * RECIBE el MusicRepository ya creado para no tener que lidiar
 * con la inicialización asíncrona de Retrofit aquí.
 */
class SearchViewModelFactory(private val repository: MusicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}