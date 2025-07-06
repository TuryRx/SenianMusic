package com.example.senianmusic.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_songs")
data class OfflineSong(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val localFilePath: String // Ruta al archivo descargado
)