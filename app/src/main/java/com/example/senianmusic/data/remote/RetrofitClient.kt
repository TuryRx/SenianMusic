package com.example.senianmusic.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var retrofit: Retrofit? = null
    private var apiService: NavidromeApiService? = null

    // Usaremos esta función para inicializar Retrofit una vez que tengamos la URL
    fun initialize(baseUrl: String) {
        // Un interceptor para ver las llamadas de red en el Logcat (muy útil para depurar)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Construimos la instancia de Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl) // La URL que el usuario introduce
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON a objetos Kotlin
            .build()

        // Creamos la implementación de nuestra interfaz de API
        apiService = retrofit?.create(NavidromeApiService::class.java)
    }

    // Usaremos esta función para obtener la instancia del servicio de API
    fun getApiService(): NavidromeApiService {
        // Lanza una excepción si se intenta usar antes de inicializar
        return apiService ?: throw IllegalStateException("RetrofitClient must be initialized first.")
    }
}