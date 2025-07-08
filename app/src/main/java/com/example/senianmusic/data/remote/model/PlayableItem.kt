package com.example.senianmusic.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Sealed class para representar cualquier cosa que pueda estar en nuestra cola de reproducción
@Parcelize
sealed class PlayableItem : Parcelable {
    @Parcelize
    data class SongItem(val song: Song) : PlayableItem()

    @Parcelize
    data class AlbumItem(val album: Album) : PlayableItem()
}