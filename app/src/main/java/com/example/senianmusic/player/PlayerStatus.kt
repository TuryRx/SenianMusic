package com.example.senianmusic.player

import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import com.example.senianmusic.data.remote.model.Song

object PlayerStatus {
    // ESTADO GENERAL
    var currentSong: Song? = null
        private set
    var isPlaying: Boolean = false
    var currentPosition: Long = 0
    var totalDuration: Long = 0

    // LISTA MAESTRA (LA FILA COMPLETA, ej: [Song, Album, Song])
    var masterPlaylist: List<Parcelable> = emptyList()
        private set
    var masterPlaylistIndex: Int = -1
        private set

    // PLAYLIST DEL ITEM ACTUAL (si es un Album, son sus canciones; si es una Song, es solo esa canción)
    var currentItemPlaylist: List<Song> = emptyList()
        private set
    var currentSongIndex: Int = -1
        private set

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

    /**
     * La nueva función principal para establecer el estado.
     * La llama PlaybackActivity después de determinar qué se va a reproducir.
     */
    fun setCurrentPlayback(
        masterList: List<Parcelable>,
        masterIndex: Int,
        songsForItem: List<Song>,
        songStartIndex: Int = 0
    ) {
        this.masterPlaylist = masterList
        this.masterPlaylistIndex = masterIndex
        this.currentItemPlaylist = songsForItem
        this.currentSongIndex = songStartIndex
        this.currentSong = currentItemPlaylist.getOrNull(songStartIndex)
        this.isPlaying = true
        notifyListeners()
    }

    fun playNextSongInCurrentItem() {
        if (hasNextSongInCurrentItem()) {
            currentSongIndex++
            currentSong = currentItemPlaylist.getOrNull(currentSongIndex)
            isPlaying = true
            notifyListeners()
        }
    }

    fun playPreviousSongInCurrentItem() {
        if (hasPreviousSongInCurrentItem()) {
            currentSongIndex--
            currentSong = currentItemPlaylist.getOrNull(currentSongIndex)
            isPlaying = true
            notifyListeners()
        }
    }

    // --- FUNCIONES DE AYUDA ---
    fun hasNextSongInCurrentItem(): Boolean {
        return currentItemPlaylist.isNotEmpty() && currentSongIndex < currentItemPlaylist.size - 1
    }

    fun hasPreviousSongInCurrentItem(): Boolean {
        return currentItemPlaylist.isNotEmpty() && currentSongIndex > 0
    }

    fun hasNextItemInMasterPlaylist(): Boolean {
        return masterPlaylist.isNotEmpty() && masterPlaylistIndex < masterPlaylist.size - 1
    }

    fun hasPreviousItemInMasterPlaylist(): Boolean {
        return masterPlaylist.isNotEmpty() && masterPlaylistIndex > 0
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying
            notifyListeners()
        }
    }

    fun updateProgress(position: Long, duration: Long) {
        if (duration > 0) {
            this.currentPosition = position
            this.totalDuration = duration
        }
    }

    fun clear() {
        currentSong = null
        isPlaying = false
        currentPosition = 0
        totalDuration = 0
        masterPlaylist = emptyList()
        masterPlaylistIndex = -1
        currentItemPlaylist = emptyList()
        currentSongIndex = -1
        notifyListeners()
    }
}