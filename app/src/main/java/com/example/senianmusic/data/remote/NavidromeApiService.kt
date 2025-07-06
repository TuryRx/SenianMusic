package com.example.senianmusic.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Modelo de datos (simplificado)
data class Artist(val id: String, val name: String)
data class Album(val id: String, val name: String, val artist: String)
// ... crea los modelos de datos que necesites a partir de la respuesta JSON

interface NavidromeApiService {
    // Ping para comprobar la conexión
    @GET("rest/ping.view")
    suspend fun ping(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<Any> // Ajusta el tipo de respuesta

    // Obtener artistas
    @GET("rest/getArtists.view")
    suspend fun getArtists(
        /* ... parámetros de autenticación ... */
    ): Response<Any> // La respuesta real es más compleja
}