package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

data class AlbumResponse(@SerializedName("subsonic-response") val subsonicResponse: SubsonicAlbumResponse)
data class SubsonicAlbumResponse(val album: AlbumWithSongs)

// Usamos un nombre diferente para no confundir con el 'Album' simple
data class AlbumWithSongs(
    val id: String,
    val name: String,
    val artist: String,
    val coverArt: String?,
    @SerializedName("song") val songList: List<Song>
)