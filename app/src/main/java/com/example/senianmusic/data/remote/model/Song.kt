package com.example.senianmusic.data.remote.model
// Asegúrate de que el package sea el correcto para tu proyecto.
// Si tu paquete es com.example.senianmusic, entonces el de arriba es correcto.

import com.google.gson.annotations.SerializedName

// Usamos @SerializedName si el nombre en el JSON de la API es diferente
// al nombre que queremos usar en nuestra clase. Es una buena práctica.

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

    // Este es un campo muy importante que necesitarás para las imágenes.
    // La API de Navidrome/Subsonic te da un ID para la carátula.
    @SerializedName("coverArt")
    val coverArtId: String? = null
) {
    // Puedes añadir una función de ayuda para construir la URL completa de la carátula.
    // Esto es muy útil para no tener que construir la URL en todas partes.
    // NOTA: Necesitarás la URL base del servidor para esto.
    fun getCoverArtUrl(baseUrl: String): String? {
        if (baseUrl.isBlank() || coverArtId.isNullOrBlank()) {
            return null
        }
        // La URL de la carátula se construye con el endpoint getCoverArt.view
        // y los parámetros de autenticación (que no incluimos aquí para simplificar).
        return "${baseUrl}rest/getCoverArt.view?v=1.16.1&c=SenianMusic&id=$coverArtId"
    }
}