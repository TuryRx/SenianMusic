package com.example.senianmusic.data.repository

import android.content.Context
import android.util.Log
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.local.dao.SongDao
import com.example.senianmusic.data.remote.NavidromeApiService
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Artist
import com.example.senianmusic.data.remote.model.SearchResult3
import com.example.senianmusic.data.remote.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.example.senianmusic.data.remote.model.ArtistWithAlbums


class MusicRepository(
    val context: Context,
    private val songDao: SongDao,
    val settingsRepository: SettingsRepository,
    private val apiService: NavidromeApiService
) {
    private val TAG = "ArtistDebug"

    // ... (El resto de tus funciones como getSession, fetchArtists, etc. se quedan igual) ...

    suspend fun fetchArtistDetails(artistId: String): ArtistWithAlbums? {
        val session = getSession() ?: return null
        Log.d(TAG, "Repo: Fetching details for artist ID: $artistId")
        return try {
            val response = apiService.getArtist(session.user, session.token, session.salt, artistId)
            if (response.isSuccessful) {
                val artistDetails = response.body()?.subsonicResponse?.artist
                Log.d(TAG, "Repo: fetchArtistDetails success. Artist: ${artistDetails?.name}, Album count: ${artistDetails?.albumList?.size}")
                artistDetails?.albumList?.forEach { album ->
                    album.coverArtUrl = album.buildCoverArtUrlForAlbum(session.baseUrl, session.user, session.token, session.salt)
                }
                artistDetails
            } else {
                Log.e(TAG, "Repo: Error fetching artist details: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Repo: Exception fetching artist details", e)
            null
        }
    }

    suspend fun fetchAllSongsByArtist(artistId: String): List<Song> {
        val session = getSession() ?: return emptyList()
        Log.d(TAG, "Repo: Fetching ALL songs for artist ID: $artistId")

        val artistDetails = fetchArtistDetails(artistId)
        val albums = artistDetails?.albumList
        if (albums.isNullOrEmpty()) {
            Log.w(TAG, "Repo: No albums found for artist $artistId. Cannot fetch songs.")
            return emptyList()
        }

        Log.d(TAG, "Repo: Found ${albums.size} albums for artist. Fetching songs from each...")

        return withContext(Dispatchers.IO) {
            val songJobs = albums.map { album ->
                async {
                    fetchAlbumDetails(album.id)
                }
            }
            val allSongs = songJobs.awaitAll().flatten().distinctBy { it.id }
            Log.d(TAG, "Repo: fetchAllSongsByArtist finished. Total unique songs: ${allSongs.size}")
            allSongs
        }
    }

    // ... El resto de tus funciones: fetchAlbumDetails, search, etc...
    // Mantenlas como estaban, no es necesario modificarlas.
    private data class SessionData(val baseUrl: String, val user: String, val token: String, val salt: String)

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

    suspend fun fetchAlbumList(type: String): List<Album> {
        return try {
            val session = getSession() ?: return emptyList()
            val response = apiService.getAlbumList2(session.user, session.token, session.salt, type, 20)
            if (response.isSuccessful) {
                val albums = response.body()?.subsonicResponse?.albumList?.albumList ?: emptyList()
                albums.forEach { album ->
                    album.coverArtUrl = album.buildCoverArtUrlForAlbum(session.baseUrl, session.user, session.token, session.salt)
                }
                albums
            } else { emptyList() }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun fetchAlbumDetails(albumId: String): List<Song> {
        return try {
            val session = getSession() ?: return emptyList()
            val response = apiService.getAlbum(session.user, session.token, session.salt, albumId)
            if (response.isSuccessful) {
                val songs = response.body()?.subsonicResponse?.album?.songList ?: emptyList()
                songs.forEach { song ->
                    song.coverArtUrl = song.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt)
                }
                songs
            } else { emptyList() }
        } catch(e: Exception) { emptyList() }
    }

    suspend fun scrobbleSong(songId: String) {
        val session = getSession() ?: return
        try {
            apiService.scrobble(session.user, session.token, session.salt, songId)
            Log.d("MusicRepository", "Scrobble exitoso para la canción ID: $songId")
        } catch (e: Exception) {
            Log.e("MusicRepository", "Falló el scrobble para la canción ID: $songId", e)
        }
    }

    suspend fun search(query: String): SearchResult3? {
        if (query.isBlank()) return null
        val session = getSession() ?: return null

        return try {
            val response = apiService.search3(session.user, session.token, session.salt, query)
            if (response.isSuccessful) {
                val result = response.body()?.subsonicResponse?.searchResult3
                result?.songList?.forEach { it.coverArtUrl = it.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt) }
                result?.albumList?.forEach { it.coverArtUrl = it.buildCoverArtUrlForAlbum(session.baseUrl, session.user, session.token, session.salt) }
                result
            } else {
                Log.e("MusicRepository", "Error en la búsqueda: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Excepción en la búsqueda", e)
            null
        }
    }
}