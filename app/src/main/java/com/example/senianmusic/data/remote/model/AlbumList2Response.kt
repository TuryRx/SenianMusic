package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

// Modelo para getAlbumList2.view
data class AlbumList2Response(@SerializedName("subsonic-response") val subsonicResponse: SubsonicAlbumList2Response)
data class SubsonicAlbumList2Response(@SerializedName("albumList2") val albumList: AlbumList)
data class AlbumList(@SerializedName("album") val albumList: List<Album>)

