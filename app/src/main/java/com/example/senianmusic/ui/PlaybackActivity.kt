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
import com.example.senianmusic.data.local.AppDatabase
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import com.example.senianmusic.databinding.ActivityPlaybackBinding
import com.example.senianmusic.player.MusicPlayer
import com.example.senianmusic.player.PlayerStatus
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

        val currentSong = PlayerStatus.currentSong
        if (currentSong == null) {
            Toast.makeText(this, "Error: No hay canción en reproducción.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateUiWithSongData(currentSong)
        updateUiFromPlayerState()
        if (PlayerStatus.isPlaying) {
            handler.post(uiUpdateRunnable)
        }

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

    private fun finishWithError() {
        Toast.makeText(this, "Error al cargar la canción.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun playSong(song: Song) {
        updateUiWithSongData(song)
        lifecycleScope.launch(Dispatchers.IO) {
            val streamUrl = getStreamUrlForSong(song)
            if (streamUrl != null) {
                withContext(Dispatchers.Main) { MusicPlayer.play(applicationContext, streamUrl) }
            } else {
                withContext(Dispatchers.Main) { Toast.makeText(applicationContext, "No se pudo obtener la URL para reproducir.", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun playNextSong() {
        PlayerStatus.playNext()
        PlayerStatus.currentSong?.let { playSong(it) }
    }

    private fun playPreviousSong() {
        PlayerStatus.playPrevious()
        PlayerStatus.currentSong?.let { playSong(it) }
    }

    private fun setupButtonListeners() {
        binding.btnPlayPause.setOnClickListener {
            val willBePlaying = !PlayerStatus.isPlaying
            PlayerStatus.updateIsPlaying(willBePlaying)
            if (willBePlaying) MusicPlayer.resume() else MusicPlayer.pause()
        }
        binding.btnNext.setOnClickListener { playNextSong() }
        binding.btnPrevious.setOnClickListener { playPreviousSong() }
        binding.btnReplay10.setOnClickListener { player?.let { it.seekTo(it.currentPosition - 10000) } }
        binding.btnForward10.setOnClickListener { player?.let { it.seekTo(it.currentPosition + 10000) } }
    }

    // --- SETUP PLAYER LISTENER ACTUALIZADO ---
    private fun setupPlayerListener() {
        playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                PlayerStatus.updateIsPlaying(isPlaying)
                updateUiFromPlayerState()
                if (isPlaying) handler.post(uiUpdateRunnable) else handler.removeCallbacks(uiUpdateRunnable)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateUiFromPlayerState()
                if (playbackState == Player.STATE_ENDED) {
                    // Primero, comprobamos si hay una siguiente canción en la playlist actual
                    if (PlayerStatus.hasNextSong()) {
                        Log.d("PlaybackActivity", "Reproduciendo siguiente canción de la playlist.")
                        playNextSong()
                    }
                    // Si no, comprobamos si estamos en una lista de reproducción de álbumes
                    else if (PlayerStatus.albumPlaylist.isNotEmpty()) {
                        val nextAlbumIndex = PlayerStatus.currentAlbumIndex + 1
                        if (nextAlbumIndex < PlayerStatus.albumPlaylist.size) {
                            Log.d("PlaybackActivity", "Fin de álbum, cargando el siguiente.")
                            val nextAlbum = PlayerStatus.albumPlaylist[nextAlbumIndex]
                            playNextAlbum(nextAlbum, nextAlbumIndex)
                        } else {
                            // Se acabaron todos los álbumes
                            Log.d("PlaybackActivity", "Fin de la lista de álbumes.")
                            Toast.makeText(applicationContext, "Fin de la lista de reproducción", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Si no se cumple ninguna condición, la música simplemente se detendrá.
                }
            }
        }
        player?.addListener(playerListener!!)
        updateUiFromPlayerState()
    }

    // --- NUEVA FUNCIÓN PARA REPRODUCIR EL SIGUIENTE ÁLBUM ---
    private fun playNextAlbum(album: Album, albumIndex: Int) {
        Toast.makeText(this, "Cargando siguiente álbum: ${album.name}", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            // Esta es una forma simplificada de obtener el repositorio.
            // En una app más grande, se usaría inyección de dependencias (Hilt/Koin).
            val repository = MusicRepository(
                applicationContext,
                AppDatabase.getDatabase(applicationContext).songDao(),
                SettingsRepository(applicationContext),
                RetrofitClient.getApiService()
            )
            val songs = repository.fetchAlbumDetails(album.id)
            if (songs.isNotEmpty()) {
                // Actualizamos el estado global con el nuevo álbum y su lista de canciones
                PlayerStatus.setAlbumPlaylist(PlayerStatus.albumPlaylist, albumIndex, songs, 0)
                // Reproducimos la primera canción del nuevo álbum
                playSong(songs[0])
            } else {
                Log.e("PlaybackActivity", "El siguiente álbum '${album.name}' no tiene canciones.")
                // Opcional: intentar saltar al siguiente álbum si este está vacío.
            }
        }
    }

    private fun updateUiFromPlayerState() {
        player?.let {
            if (it.duration > 0) {
                binding.tvTotalTime.text = formatDuration(it.duration)
                binding.seekBar.max = it.duration.toInt()
            }
            updateSeekBar()
            binding.btnPlayPause.setImageResource(
                if (PlayerStatus.isPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle
            )
        }
    }

    private fun updateSeekBar() {
        player?.let {
            PlayerStatus.updateProgress(it.currentPosition, it.duration)
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

    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}