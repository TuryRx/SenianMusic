package com.example.senianmusic.data.remote.model

import com.google.gson.annotations.SerializedName

data class ArtistsResponse(@SerializedName("subsonic-response") val subsonicResponse: SubsonicArtistsResponse)
data class SubsonicArtistsResponse(val artists: Artists)
data class Artists(val index: List<Index>)
data class Index(val name: String, val artist: List<Artist>)