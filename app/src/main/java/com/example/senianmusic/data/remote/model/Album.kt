package com.example.senianmusic.data.remote.model

import android.os.Parcelable // <-- Importa Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize // <-- Importa Parcelize

@Parcelize // <-- ¡AÑADE ESTA ANOTACIÓN!
data class Album(
    val id: String,
    val name: String,
    val artist: String,
    @SerializedName("coverArt") val coverArtId: String?,
    val songCount: Int,
    val created: String,
    val artistId: String,
    @Transient var coverArtUrl: String? = null
) : Parcelable { // <-- ¡AÑADE LA HERENCIA!
    // El método buildCoverArtUrlForAlbum se queda igual
    fun buildCoverArtUrlForAlbum(baseUrl: String, user: String, token: String, salt: String): String? {
        if (coverArtId.isNullOrBlank()) return null
        return "${baseUrl}rest/getCoverArt.view?id=$coverArtId&u=$user&t=$token&s=$salt&v=1.16.1&c=senianMusic"
    }
}