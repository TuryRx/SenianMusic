// en .../data/remote/NavidromeApiService.kt
package com.example.senianmusic.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NavidromeApiService {
    @GET("rest/ping.view")
    suspend fun ping(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "SenianMusic",
        @Query("f") format: String = "json"
    ): Response<Any>

    @GET("rest/getArtists.view")
    suspend fun getArtists(/* ... */): Response<Any>
}