// en NavidromeApiService.kt
package com.example.senianmusic.data.remote

// Importa los nuevos modelos de respuesta
import com.example.senianmusic.data.remote.model.ArtistsResponse
import com.example.senianmusic.data.remote.model.RandomSongsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NavidromeApiService {
    // ... ping ...

    // La respuesta ahora usa un modelo de datos específico
    @GET("rest/getArtists.view")
    suspend fun getArtists(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<ArtistsResponse>

    // Nuevo endpoint para obtener canciones aleatorias
    @GET("rest/getRandomSongs.view")
    suspend fun getRandomSongs(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("size") size: Int = 50, // Cuántas canciones pedir
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<RandomSongsResponse>
}