package com.example.senianmusic.data.repository

import android.content.Context
import android.util.Log
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.local.dao.SongDao
import com.example.senianmusic.data.remote.NavidromeApiService
import com.example.senianmusic.data.remote.model.Artist
import com.example.senianmusic.data.remote.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MusicRepository(
    val context: Context,
    private val songDao: SongDao,
    val settingsRepository: SettingsRepository,
    private val apiService: NavidromeApiService
) {

    private suspend fun getSession(): SessionData? {
        return try {
            val baseUrl = settingsRepository.serverUrlFlow.first()
            val user = settingsRepository.usernameFlow.first()
            val token = settingsRepository.tokenFlow.first()
            val salt = settingsRepository.saltFlow.first()
            if (baseUrl != null && user != null && token != null && salt != null) {
                SessionData(baseUrl, user, token, salt)
            } else {
                Log.e("MusicRepository", "Faltan datos de sesión (URL, user, token o salt).")
                null
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción al obtener datos de sesión", e)
            null
        }
    }

    private data class SessionData(val baseUrl: String, val user: String, val token: String, val salt: String)

    // --- FUNCIÓN CORREGIDA ---
    suspend fun fetchArtists(): List<Artist> {
        return try {
            val session = getSession() ?: return emptyList()
            val response = apiService.getArtists(session.user, session.token, session.salt)
            if (response.isSuccessful) {
                response.body()?.subsonicResponse?.artists?.index?.flatMap { it.artist } ?: emptyList()
            } else {
                Log.e("MusicRepository", "Error al obtener artistas: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción al obtener artistas", e)
            emptyList()
        }
    }

    suspend fun fetchRandomSongs(): List<Song> {
        return try {
            val session = getSession() ?: return emptyList()
            val response = apiService.getRandomSongs(session.user, session.token, session.salt, size = 50)
            if (response.isSuccessful) {
                val songsFromApi = response.body()?.subsonicResponse?.randomSongs?.songList ?: emptyList()
                songsFromApi.forEach { it.coverArtUrl = it.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt) }
                songsFromApi
            } else {
                Log.e("MusicRepository", "Error al obtener canciones aleatorias: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción al obtener canciones aleatorias", e)
            emptyList()
        }
    }

    suspend fun fetchSongsFromAlbumList(type: String): List<Song> {
        return try {
            val session = getSession() ?: return emptyList()
            val albumListResponse = apiService.getAlbumList2(session.user, session.token, session.salt, type, 10)
            if (!albumListResponse.isSuccessful) {
                Log.e("MusicRepository", "Error en getAlbumList2 ($type): ${albumListResponse.code()}")
                return emptyList()
            }

            val albums = albumListResponse.body()?.subsonicResponse?.albumList?.albumList ?: return emptyList()
            if (albums.isEmpty()) return emptyList()

            withContext(Dispatchers.IO) {
                val songJobs = albums.map { album ->
                    async {
                        val albumResponse = apiService.getAlbum(session.user, session.token, session.salt, album.id)
                        if (albumResponse.isSuccessful) {
                            albumResponse.body()?.subsonicResponse?.album?.songList ?: emptyList()
                        } else {
                            emptyList()
                        }
                    }
                }
                val allSongs = songJobs.awaitAll().flatten()
                allSongs.forEach { it.coverArtUrl = it.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt) }
                allSongs
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción en fetchSongsFromAlbumList ($type)", e)
            emptyList()
        }
    }

    suspend fun fetchTopSongs(): List<Song> {
        return try {
            val session = getSession() ?: return emptyList()
            val response = apiService.getTopSongs(session.user, session.token, session.salt, 50)
            if (response.isSuccessful) {
                val topSongs = response.body()?.subsonicResponse?.topSongs?.songList ?: emptyList()
                topSongs.forEach { it.coverArtUrl = it.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt) }
                topSongs
            } else {
                Log.e("MusicRepository", "Error en fetchTopSongs: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción en fetchTopSongs", e)
            emptyList()
        }
    }

    suspend fun getStreamUrlForSong(song: Song): String? {
        val session = getSession() ?: return null
        return song.getStreamUrl(session.baseUrl, session.user, session.token, session.salt)
    }

    suspend fun getCoverUrlForSong(song: Song): String? {
        val session = getSession() ?: return null
        if (!song.coverArtUrl.isNullOrBlank()) {
            return song.coverArtUrl
        }
        return song.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt)
    }

    fun startSongDownload(song: Song) {
        Log.d("MusicRepository", "Solicitando descarga para la canción: ${song.title}")
        // TODO: Implementar lógica de descarga.
    }

    suspend fun searchSongs(query: String): List<Song> {
        if (query.isBlank()) return emptyList()

        return try {
            val session = getSession() ?: return emptyList()
            Log.d("MusicRepository", "Buscando canciones con la consulta: '$query'")
            val response = apiService.search3(
                user = session.user,
                token = session.token,
                salt = session.salt,
                query = query
            )

            if (response.isSuccessful) {
                val songs = response.body()?.subsonicResponse?.searchResult3?.songList ?: emptyList()
                Log.d("MusicRepository", "Encontradas ${songs.size} canciones.")
                // Construimos las URLs de las carátulas para que se muestren en los resultados
                songs.forEach { song ->
                    song.coverArtUrl = song.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt)
                }
                songs
            } else {
                Log.e("MusicRepository", "Error al buscar canciones: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción al buscar canciones", e)
            emptyList()
        }
    }

}