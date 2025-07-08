package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

// Modelo para getTopSongs.view
data class TopSongsResponse(@SerializedName("subsonic-response") val subsonicResponse: SubsonicTopSongsResponse)
data class SubsonicTopSongsResponse(@SerializedName("topSongs") val topSongs: TopSongs)
data class TopSongs(@SerializedName("song") val songList: List<Song>)