package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Un modelo de respuesta genérico para llamadas a la API de Subsonic
 * que solo devuelven un estado, como 'ping' o 'scrobble'.
 */
data class SubsonicResponse(
    @SerializedName("subsonic-response")
    val subsonicResponse: BaseResponse
)

data class BaseResponse(
    @SerializedName("status")
    val status: String, // Será "ok" o "failed"

    @SerializedName("version")
    val version: String,

    // El campo 'error' es opcional, solo aparecerá si status es "failed"
    @SerializedName("error")
    val error: Error? = null
)

data class Error(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String
)