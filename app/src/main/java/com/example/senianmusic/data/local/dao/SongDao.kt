package com.example.senianmusic.data.local.dao

import androidx.room.Dao // <-- 1. ¡IMPORT CRUCIAL!
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.senianmusic.data.local.model.OfflineSong
import kotlinx.coroutines.flow.Flow

@Dao // <-- 2. ¡ANOTACIÓN OBLIGATORIA!
interface SongDao { // <-- 3. DEBE ser una 'interface' (o abstract class)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: OfflineSong)

    @Query("SELECT * FROM offline_songs")
    fun getAllOfflineSongs(): Flow<List<OfflineSong>>

    // Ejemplo de otras funciones que podrías tener
    @Query("SELECT * FROM offline_songs WHERE id = :songId")
    suspend fun getSongById(songId: String): OfflineSong?

    @Query("DELETE FROM offline_songs WHERE id = :songId")
    suspend fun deleteSongById(songId: String)
}