package com.example.senianmusic.data.repository

import android.content.Context
import com.example.senianmusic.data.local.SettingsRepository // 1. IMPORTANTE: Necesitas este repositorio
import com.example.senianmusic.data.local.dao.SongDao
import com.example.senianmusic.data.local.model.OfflineSong
import com.example.senianmusic.data.remote.NavidromeApiService
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.remote.model.Song // 2. IMPORTANTE: Modelo de datos de la API
import com.example.senianmusic.util.DownloadHelper
import kotlinx.coroutines.flow.first

// 3. CAMBIO IMPORTANTE: El constructor ahora necesita más cosas
class MusicRepository(
    private val songDao: SongDao,
    private val settingsRepository: SettingsRepository,
    private val context: Context // El contexto es necesario para DownloadHelper
) {

    // Se mantiene igual, obtiene la instancia de Retrofit
    private val apiService: NavidromeApiService = RetrofitClient.getApiService()

    // --- FUNCIÓN DE EJEMPLO MEJORADA PARA OBTENER DATOS DE LA API ---
    // En lugar de 'Artist', usaré un ejemplo con 'Album' que es más común
    // Asegúrate de tener un modelo de datos 'Album' para la respuesta de la API
    // suspend fun fetchAlbums(): List<Album> {
    //     try {
    //         // Necesitarás los parámetros de autenticación de tu SettingsRepository
    //         val user = settingsRepository.usernameFlow.first() ?: ""
    //         val token = settingsRepository.tokenFlow.first() ?: ""
    //         val salt = "someRandomSalt" // El que usaste para crear el token
    //
    //         val response = apiService.getAlbumList(/* aquí pasas user, token, salt, etc. */)
    //         if (response.isSuccessful) {
    //             return response.body()?.subsonicResponse?.albumList?.album ?: emptyList()
    //         }
    //     } catch (e: Exception) {
    //         // Loggear el error es una buena práctica
    //         Log.e("MusicRepository", "Error fetching albums", e)
    //     }
    //     return emptyList()
    // }

    // --- LÓGICA DE DESCARGA (LA PARTE NUEVA Y COMPLETA) ---
    suspend fun startSongDownload(song: Song) {
        try {
            // 4. Obtiene los datos de sesión necesarios del SettingsRepository
            val baseUrl = settingsRepository.serverUrlFlow.first()
            val user = settingsRepository.usernameFlow.first()
            val token = settingsRepository.tokenFlow.first()

            // Valida que tengas todos los datos necesarios antes de proceder
            if (baseUrl.isNullOrBlank() || user.isNullOrBlank() || token.isNullOrBlank()) {
                // Puedes lanzar una excepción o manejar el error como prefieras
                throw IllegalStateException("User not logged in or server not configured.")
            }

            // 5. Construye la URL de descarga para la API de Subsonic/Navidrome
            val downloadUrl = "${baseUrl}rest/stream.view?" +
                    "u=$user&t=$token&s=someRandomSalt&v=1.16.1&c=SenianMusic&id=${song.id}"

            // 6. Instancia y usa tu clase de ayuda para las descargas
            val downloadHelper = DownloadHelper(context)

            val downloadId = downloadHelper.startDownload(
                songUrl = downloadUrl,
                songTitle = song.title,
                albumName = song.album
            )

            // 7. Crea el objeto para la base de datos local y guárdalo
            // Esto marca la canción como "pendiente de descarga" en tu base de datos
            val offlineSong = OfflineSong(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                localFilePath = null, // La ruta aún no existe
                // Campos adicionales que deberías añadir a tu entidad 'OfflineSong'
                downloadManagerId = downloadId,
                downloadStatus = "PENDING"
            )
            songDao.insertSong(offlineSong)

        } catch (e: Exception) {
            // Maneja cualquier error que pueda ocurrir durante el proceso
            // Log.e("MusicRepository", "Failed to start download for song ${song.id}", e)
        }
    }

    // --- OTRAS FUNCIONES DEL REPOSITORIO ---

    // Función para obtener todas las canciones descargadas desde la base de datos local
    fun getOfflineSongs() = songDao.getAllOfflineSongs()
}