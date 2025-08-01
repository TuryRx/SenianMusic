// en NavidromeApiService.kt
package com.example.senianmusic.data.remote
import com.example.senianmusic.data.remote.model.AlbumList2Response // ¡NUEVO!
import com.example.senianmusic.data.remote.model.TopSongsResponse   // ¡NUEVO!
import com.example.senianmusic.data.remote.model.AlbumResponse
import com.example.senianmusic.data.remote.model.SubsonicResponse
import com.example.senianmusic.data.remote.model.ArtistDetailResponse

// Importa los nuevos modelos de respuesta
import com.example.senianmusic.data.remote.model.ArtistsResponse
import com.example.senianmusic.data.remote.model.RandomSongsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

import com.example.senianmusic.data.remote.model.Search3Response

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

    // Para "Agregadas Recientemente" y "Escuchadas Recientemente" (álbumes)
    @GET("rest/getAlbumList2.view")
    suspend fun getAlbumList2(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("type") type: String, // "recent", "frequent", "random", etc.
        @Query("size") size: Int = 20,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<AlbumList2Response>

    // Para "Recomendados" (las canciones más escuchadas, es una buena aproximación)
    @GET("rest/getTopSongs.view")
    suspend fun getTopSongs(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("count") count: Int = 50,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<TopSongsResponse>

    // --- ¡NUEVO ENDPOINT! ---
    @GET("rest/getAlbum.view")
    suspend fun getAlbum(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String, // El ID del álbum que queremos
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<AlbumResponse>

    // --- ¡NUEVO ENDPOINT DE BÚSQUEDA! ---
    @GET("rest/search3.view")
    suspend fun search3(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("query") query: String,
        @Query("songCount") songCount: Int = 20, // Pedimos un número razonable
        @Query("albumCount") albumCount: Int = 10, // Pedimos álbumes
        @Query("artistCount") artistCount: Int = 5, // y artistas
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<Search3Response>

    // --- ¡AÑADE ESTA NUEVA FUNCIÓN AQUÍ! ---
    @GET("rest/scrobble.view")
    suspend fun scrobble(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String, // El ID de la canción que se reprodujo
        @Query("submission") submission: Boolean = true, // Es una buena práctica indicar que es una sumisión
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<SubsonicResponse> // Usaremos un modelo de respuesta genérico

    @GET("rest/ping.view")
    suspend fun ping(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<SubsonicResponse> // Usa tu clase de respuesta base.

    // --- ¡AÑADE ESTE NUEVO ENDPOINT! ---
    @GET("rest/getArtist.view")
    suspend fun getArtist(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String, // El ID del artista que queremos
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<ArtistDetailResponse>

}