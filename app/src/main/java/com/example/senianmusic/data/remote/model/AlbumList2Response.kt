package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

// Modelo para getAlbumList2.view
data class AlbumList2Response(@SerializedName("subsonic-response") val subsonicResponse: SubsonicAlbumList2Response)
data class SubsonicAlbumList2Response(@SerializedName("albumList2") val albumList: AlbumList)
data class AlbumList(@SerializedName("album") val albumList: List<Album>)

// Modelo de un Album (si no lo tienes ya)
// Un Album es como una canción pero con menos detalles, lo importante es que tenga ID para obtener sus canciones
data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val coverArt: String?, // ID de la carátula
    val songCount: Int,
    val created: String,
    val artistId: String,
    // Puedes añadir más campos si los necesitas
)