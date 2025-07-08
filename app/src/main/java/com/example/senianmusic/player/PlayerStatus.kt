package com.example.senianmusic.player

import android.os.Handler
import android.os.Looper
import com.example.senianmusic.data.remote.model.Album // ¡Importación necesaria!
import com.example.senianmusic.data.remote.model.Song

object PlayerStatus {
    // --- ESTADO DE CANCIÓN ACTUAL ---
    var currentSong: Song? = null
    var isPlaying: Boolean = false
    var currentPosition: Long = 0
    var totalDuration: Long = 0
    var playlist: List<Song> = emptyList()
    var currentSongIndex: Int = -1

    // --- NUEVO: ESTADO DE LA PLAYLIST DE ÁLBUMES ---
    var albumPlaylist: List<Album> = emptyList()
        private set
    var currentAlbumIndex: Int = -1
        private set
    // --- FIN NUEVO ---

    private val listeners = mutableListOf<() -> Unit>()
    private val handler = Handler(Looper.getMainLooper())

    fun addListener(listener: () -> Unit) {
        if (!listeners.contains(listener)) listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        handler.post { listeners.forEach { it.invoke() } }
    }

    // --- MÉTODOS DE CONTROL DE ESTADO ---

    fun setPlaylist(newPlaylist: List<Song>, startIndex: Int) {
        // Al establecer una playlist de canciones normal, limpiamos el estado de los álbumes.
        clearAlbumPlaylist()
        this.playlist = newPlaylist
        this.currentSongIndex = startIndex
        this.currentSong = playlist.getOrNull(startIndex)
        this.isPlaying = true
        notifyListeners()
    }

    // --- NUEVA FUNCIÓN PARA ESTABLECER PLAYLIST DE ÁLBUMES ---
    fun setAlbumPlaylist(albums: List<Album>, albumIndex: Int, songsOfFirstAlbum: List<Song>, songIndex: Int) {
        this.albumPlaylist = albums
        this.currentAlbumIndex = albumIndex

        // El resto es igual que setPlaylist, pero sin limpiar el estado del álbum
        this.playlist = songsOfFirstAlbum
        this.currentSongIndex = songIndex
        this.currentSong = playlist.getOrNull(songIndex)
        this.isPlaying = true
        notifyListeners()
    }
    // --- FIN NUEVO ---

    fun playNext() {
        if (playlist.isEmpty()) return
        currentSongIndex = (currentSongIndex + 1) % playlist.size
        currentSong = playlist.getOrNull(currentSongIndex)
        isPlaying = true
        notifyListeners()
    }

    fun playPrevious() {
        if (playlist.isEmpty()) return
        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else playlist.size - 1
        currentSong = playlist.getOrNull(currentSongIndex)
        isPlaying = true
        notifyListeners()
    }

    // --- NUEVA FUNCIÓN DE AYUDA ---
    fun hasNextSong(): Boolean {
        return playlist.isNotEmpty() && currentSongIndex < playlist.size - 1
    }
    // --- FIN NUEVO ---

    fun updateIsPlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        notifyListeners()
    }

    fun updateProgress(position: Long, duration: Long) {
        if (duration > 0) {
            this.currentPosition = position
            this.totalDuration = duration
        }
    }

    // --- NUEVA FUNCIÓN PARA LIMPIAR ESTADO DE ÁLBUMES ---
    private fun clearAlbumPlaylist() {
        this.albumPlaylist = emptyList()
        this.currentAlbumIndex = -1
    }
    // --- FIN NUEVO ---

    fun clear() {
        clearAlbumPlaylist() // Nos aseguramos de limpiar también los álbumes
        currentSong = null
        isPlaying = false
        currentPosition = 0
        totalDuration = 0
        playlist = emptyList()
        currentSongIndex = -1
        notifyListeners()
    }
}