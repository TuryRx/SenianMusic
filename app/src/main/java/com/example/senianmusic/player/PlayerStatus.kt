package com.example.senianmusic.player

import android.os.Handler
import android.os.Looper
import com.example.senianmusic.data.remote.model.Song

/**
 * Objeto Singleton que actúa como una fuente de verdad global y robusta
 * para el estado actual de la sesión de reproducción.
 */
object PlayerStatus {
    // --- ESTADO COMPLETO DE LA REPRODUCCIÓN ---
    var currentSong: Song? = null
    var isPlaying: Boolean = false
    var currentPosition: Long = 0
    var totalDuration: Long = 0
    var playlist: List<Song> = emptyList() // <-- AHORA ES LA FUENTE DE VERDAD
    var currentSongIndex: Int = -1         // <-- AHORA ES LA FUENTE DE VERDAD

    private val listeners = mutableListOf<() -> Unit>()
    private val handler = Handler(Looper.getMainLooper())

    fun addListener(listener: () -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        handler.post { listeners.forEach { it.invoke() } }
    }

    // --- MÉTODOS DE CONTROL DE ESTADO ---

    // Inicia una nueva sesión de reproducción. A ser llamado desde MainFragment.
    fun setPlaylist(newPlaylist: List<Song>, startIndex: Int) {
        this.playlist = newPlaylist
        this.currentSongIndex = startIndex
        this.currentSong = playlist.getOrNull(startIndex)
        this.isPlaying = true // Asumimos que la reproducción comienza inmediatamente
        notifyListeners()
    }

    // Cambia a la siguiente canción y actualiza el estado
    fun playNext() {
        if (playlist.isEmpty()) return
        currentSongIndex = (currentSongIndex + 1) % playlist.size
        currentSong = playlist.getOrNull(currentSongIndex)
        isPlaying = true
        notifyListeners()
    }

    // Cambia a la canción anterior y actualiza el estado
    fun playPrevious() {
        if (playlist.isEmpty()) return
        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else playlist.size - 1
        currentSong = playlist.getOrNull(currentSongIndex)
        isPlaying = true
        notifyListeners()
    }

    // Actualiza solo el estado de reproducción (play/pausa)
    fun updateIsPlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        notifyListeners()
    }

    // Actualiza solo el progreso. No notifica para no sobrecargar la UI.
    fun updateProgress(position: Long, duration: Long) {
        if (duration > 0) {
            this.currentPosition = position
            this.totalDuration = duration
        }
    }

    // Limpia todo el estado cuando la sesión de reproducción termina.
    fun clear() {
        currentSong = null
        isPlaying = false
        currentPosition = 0
        totalDuration = 0
        playlist = emptyList()
        currentSongIndex = -1
        notifyListeners()
    }
}