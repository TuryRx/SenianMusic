// com/example/senianmusic/data/remote/model/ArtistDetailResponse.kt
package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

// Modelo para la llamada getArtist.view
data class ArtistDetailResponse(@SerializedName("subsonic-response") val subsonicResponse: SubsonicArtistDetailResponse)
data class SubsonicArtistDetailResponse(@SerializedName("artist") val artist: ArtistWithAlbums)

data class ArtistWithAlbums(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("coverArt") val coverArt: String?, // ID de la imagen
    @SerializedName("albumCount") val albumCount: Int,
    @SerializedName("album") val albumList: List<Album>
)