package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

data class Search3Response(@SerializedName("subsonic-response") val subsonicResponse: SubsonicSearch3Response)
data class SubsonicSearch3Response(@SerializedName("searchResult3") val searchResult3: SearchResult3)

// El contenedor de los resultados ahora tiene los 3 tipos de listas
data class SearchResult3(
    @SerializedName("artist") val artistList: List<Artist>?,
    @SerializedName("album") val albumList: List<Album>?,
    @SerializedName("song") val songList: List<Song>?
)