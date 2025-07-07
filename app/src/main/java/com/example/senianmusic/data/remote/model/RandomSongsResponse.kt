package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

data class RandomSongsResponse(@SerializedName("subsonic-response") val subsonicResponse: SubsonicRandomSongsResponse)
data class SubsonicRandomSongsResponse(@SerializedName("randomSongs") val randomSongs: RandomSongs)
data class RandomSongs(@SerializedName("song") val songList: List<Song>)