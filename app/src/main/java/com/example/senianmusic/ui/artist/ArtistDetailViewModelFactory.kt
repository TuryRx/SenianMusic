// Crea este nuevo archivo en: com/example/senianmusic/ui/artist/
package com.example.senianmusic.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.senianmusic.data.repository.MusicRepository

class ArtistDetailViewModelFactory(private val repository: MusicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArtistDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}