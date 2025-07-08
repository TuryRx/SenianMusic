package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

// Modelo para la respuesta de search3.view
data class Search3Response(@SerializedName("subsonic-response") val subsonicResponse: SubsonicSearch3Response)
data class SubsonicSearch3Response(@SerializedName("searchResult3") val searchResult3: SearchResult3)

// El contenedor de los resultados
data class SearchResult3(
    // La respuesta puede incluir artistas, álbumes y canciones.
    // Por ahora solo nos interesan las canciones.
    @SerializedName("song") val songList: List<Song>? // La lista puede no venir si no hay resultados
)