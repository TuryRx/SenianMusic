package com.example.senianmusic.data.remote.model

import android.os.Parcelable // <-- IMPORT AÑADIDO
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize // <-- IMPORT AÑADIDO

@Parcelize // <-- ANOTACIÓN AÑADIDA
data class Song(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: String,

    @SerializedName("album")
    val album: String,

    @SerializedName("duration")
    val duration: Int,

    @SerializedName("coverArt")
    val coverArtId: String? = null,

    @Transient var coverArtUrl: String? = null
) : Parcelable { // <-- IMPLEMENTACIÓN AÑADIDA

    fun buildCoverArtUrl(baseUrl: String, user: String, token: String, salt: String): String? {
        if (baseUrl.isBlank() || coverArtId.isNullOrBlank()) {
            return null
        }
        return "${baseUrl}rest/getCoverArt.view?v=1.16.1&c=SenianMusic&f=json&u=$user&t=$token&s=$salt&id=$coverArtId"
    }

    fun getStreamUrl(baseUrl: String, user: String, token: String, salt: String): String {
        return "${baseUrl}rest/stream.view?v=1.16.1&c=SenianMusic&u=$user&t=$token&s=$salt&id=$id"
    }
}