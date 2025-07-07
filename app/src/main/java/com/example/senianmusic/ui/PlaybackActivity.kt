package com.example.senianmusic.ui.playback

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.senianmusic.R
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.databinding.ActivityPlaybackBinding
import com.example.senianmusic.player.MusicPlayer
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PlaybackActivity : FragmentActivity() {

    private lateinit var binding: ActivityPlaybackBinding
    private lateinit var settingsRepository: SettingsRepository
    private var player: Player? = null
    private var playerListener: Player.Listener? = null
    private val handler = Handler(Looper.getMainLooper())

    // --- PROPIEDADES DE LA PLAYLIST RESTAURADAS ---
    private var playlist: ArrayList<Song> = ArrayList()
    private var currentSongIndex: Int = -1

    private val uiUpdateRunnable: Runnable = object : Runnable {
        override fun run() {
            updateSeekBar()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(applicationContext)
        player = MusicPlayer.getInstance(this)

        loadPlaylistAndPlay() // Cambiamos el nombre para que sea más claro
        setupButtonListeners()
        setupSeekBarListener()
    }

    override fun onStart() {
        super.onStart()
        setupPlayerListener()
    }

    override fun onStop() {
        super.onStop()
        playerListener?.let { player?.removeListener(it) }
        handler.removeCallbacks(uiUpdateRunnable)
    }

    private fun loadPlaylistAndPlay() {
        // Recibimos la lista y el índice del Intent (como lo envía MainFragment)
        playlist = intent.getParcelableArrayListExtra("PLAYLIST") ?: arrayListOf()
        currentSongIndex = intent.getIntExtra("CURRENT_SONG_INDEX", -1)

        if (currentSongIndex != -1 && playlist.isNotEmpty()) {
            val songToPlay = playlist[currentSongIndex]
            playSong(songToPlay)
        } else {
            Log.e("PlaybackActivity", "No se recibió una playlist o un índice válido.")
            Toast.makeText(this, "Error al cargar la canción.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun playSong(song: Song) {
        updateUiWithSongData(song)
        lifecycleScope.launch(Dispatchers.IO) {
            val streamUrl = getStreamUrlForSong(song)
            if (streamUrl != null) {
                withContext(Dispatchers.Main) {
                    MusicPlayer.play(applicationContext, streamUrl)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "No se pudo obtener la URL para reproducir", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateUiWithSongData(song: Song) {
        binding.tvSongTitle.text = song.title
        binding.tvSongArtist.text = song.artist
        lifecycleScope.launch(Dispatchers.IO) {
            val coverUrl = getCoverUrlForSong(song)
            withContext(Dispatchers.Main) {
                Glide.with(this@PlaybackActivity).load(coverUrl).placeholder(R.drawable.movie).into(binding.ivCoverArt)
                Glide.with(this@PlaybackActivity).load(coverUrl)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                    .transition(DrawableTransitionOptions.withCrossFade()).into(binding.ivBackground)
            }
        }
    }

    private suspend fun getStreamUrlForSong(song: Song): String? {
        val session = getSessionData() ?: return null
        return song.getStreamUrl(session.baseUrl, session.user, session.token, session.salt)
    }

    private suspend fun getCoverUrlForSong(song: Song): String? {
        val session = getSessionData() ?: return null
        return song.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt)
    }

    private suspend fun getSessionData(): SessionData? {
        return try {
            val baseUrl = settingsRepository.serverUrlFlow.first()
            val user = settingsRepository.usernameFlow.first()
            val token = settingsRepository.tokenFlow.first()
            val salt = settingsRepository.saltFlow.first()
            if (baseUrl != null && user != null && token != null && salt != null) {
                SessionData(baseUrl, user, token, salt)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private data class SessionData(val baseUrl: String, val user: String, val token: String, val salt: String)

    private fun setupButtonListeners() {
        binding.btnPlayPause.setOnClickListener {
            if (player?.isPlaying == true) MusicPlayer.pause() else MusicPlayer.resume()
        }
        binding.btnNext.setOnClickListener { playNextSong() }
        binding.btnPrevious.setOnClickListener { playPreviousSong() }

        binding.btnReplay10.setOnClickListener {
            player?.let { it.seekTo(it.currentPosition - 10000) } // Retrocede 10 segundos
        }
        binding.btnForward10.setOnClickListener {
            player?.let { it.seekTo(it.currentPosition + 10000) } // Adelanta 10 segundos
        }
    }

    private fun playNextSong() {
        if (playlist.isEmpty()) return
        currentSongIndex = (currentSongIndex + 1) % playlist.size // Lógica circular
        playSong(playlist[currentSongIndex])
    }

    private fun playPreviousSong() {
        if (playlist.isEmpty()) return
        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else playlist.size - 1 // Lógica circular
        playSong(playlist[currentSongIndex])
    }

    private fun setupPlayerListener() {
        playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateUiFromPlayerState()
                if (isPlaying) handler.post(uiUpdateRunnable) else handler.removeCallbacks(uiUpdateRunnable)
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateUiFromPlayerState()
                if (playbackState == Player.STATE_ENDED) {
                    // Cuando una canción termina, pasa a la siguiente automáticamente.
                    playNextSong()
                }
            }
        }
        player?.addListener(playerListener!!)
        updateUiFromPlayerState()
    }

    private fun updateUiFromPlayerState() {
        player?.let {
            if (it.duration > 0) {
                binding.tvTotalTime.text = formatDuration(it.duration)
                binding.seekBar.max = it.duration.toInt()
            }
            updateSeekBar()
            // --- LÍNEA CORREGIDA ---
            binding.btnPlayPause.setImageResource(
                if (it.isPlaying) R.drawable.ic_pause_circle
                else R.drawable.ic_play_circle
            )
        }
    }

    private fun updateSeekBar() {
        player?.let {
            binding.seekBar.progress = it.currentPosition.toInt()
            binding.tvCurrentTime.text = formatDuration(it.currentPosition)
        }
    }

    private fun setupSeekBarListener() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player?.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { handler.removeCallbacks(uiUpdateRunnable) }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { handler.post(uiUpdateRunnable) }
        })
    }

    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}