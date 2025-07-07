package com.example.senianmusic.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

object MusicPlayer {

    private var exoPlayer: ExoPlayer? = null

    /**
     * Obtiene la instancia única de ExoPlayer. La crea si no existe.
     * Esta función ahora es pública.
     */
    fun getInstance(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        return exoPlayer!!
    }

    fun play(context: Context, streamUrl: String) {
        val player = getInstance(context)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val mediaSource = ProgressiveMediaSource.Factory(httpDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(streamUrl))

        player.stop()
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    fun pause() {
        exoPlayer?.playWhenReady = false
    }

    fun resume() {
        exoPlayer?.playWhenReady = true
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}