package com.example.senianmusic.ui.playback

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
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
import com.example.senianmusic.player.PlaybackController // <-- IMPORTAMOS EL CONTROLADOR
import com.example.senianmusic.player.PlayerStatus
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PlaybackActivity : FragmentActivity() {

    companion object {
        const val ACTION_START_PLAYBACK = "com.example.senianmusic.ACTION_START_PLAYBACK"
        const val EXTRA_MASTER_PLAYLIST = "EXTRA_MASTER_PLAYLIST"
        const val EXTRA_START_INDEX = "EXTRA_START_INDEX"
    }

    private lateinit var binding: ActivityPlaybackBinding
    private lateinit var settingsRepository: SettingsRepository // <-- Todavía la necesitamos para la UI
    private var player: Player? = null
    private var playerListener: Player.Listener? = null
    private val handler = Handler(Looper.getMainLooper())

    // Este runnable para actualizar la barra de progreso se queda igual
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

        // Verificamos si es un nuevo inicio de reproducción o si la actividad se está recreando
        if (intent.action == ACTION_START_PLAYBACK) {
            handleNewPlaybackIntent(intent)
        } else if (PlayerStatus.currentSong != null) {
            // Si ya hay una canción sonando (ej. al rotar la pantalla o volver), solo actualizamos la UI
            updateUiWithSongData(PlayerStatus.currentSong!!)
            updateUiFromPlayerState()
        } else {
            finishWithError("No hay canción para reproducir.")
            return
        }

        setupButtonListeners()
        setupSeekBarListener()
    }

    private fun handleNewPlaybackIntent(intent: Intent) {
        val masterPlaylist: ArrayList<Parcelable>? = intent.getParcelableArrayListExtra(EXTRA_MASTER_PLAYLIST)
        val startIndex = intent.getIntExtra(EXTRA_START_INDEX, -1)

        if (masterPlaylist.isNullOrEmpty() || startIndex == -1) {
            finishWithError("Error al iniciar la lista de reproducción.")
            return
        }

        // Delegamos el inicio de la reproducción al controlador
        PlaybackController.startPlayback(masterPlaylist, startIndex)
    }

    private fun setupButtonListeners() {
        binding.btnPlayPause.setOnClickListener {
            val willBePlaying = !PlayerStatus.isPlaying
            PlayerStatus.updateIsPlaying(willBePlaying)
            if (willBePlaying) MusicPlayer.resume() else MusicPlayer.pause()
        }

        // Los botones de control ahora simplemente le dicen al controlador qué hacer
        binding.btnNext.setOnClickListener { PlaybackController.goToNext() }
        binding.btnPrevious.setOnClickListener { PlaybackController.goToPrevious() }

        // Estos botones interactúan directamente con el reproductor y se quedan igual
        binding.btnReplay10.setOnClickListener { player?.let { it.seekTo(it.currentPosition - 10000) } }
        binding.btnForward10.setOnClickListener { player?.let { it.seekTo(it.currentPosition + 10000) } }
    }

    override fun onStart() {
        super.onStart()
        setupPlayerListener()
        // Nos aseguramos de que el listener de la UI se active si ya está sonando
        if (PlayerStatus.isPlaying) {
            handler.post(uiUpdateRunnable)
        }
    }

    override fun onStop() {
        super.onStop()
        playerListener?.let { player?.removeListener(it) }
        handler.removeCallbacks(uiUpdateRunnable)
    }

    private fun setupPlayerListener() {
        // El listener ahora es más simple, solo reacciona a los cambios
        playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                PlayerStatus.updateIsPlaying(isPlaying)
                updateUiFromPlayerState()
                if (isPlaying) handler.post(uiUpdateRunnable) else handler.removeCallbacks(uiUpdateRunnable)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateUiFromPlayerState()
                if (playbackState == Player.STATE_ENDED) {
                    // Cuando una canción termina, le decimos al controlador que vaya a la siguiente
                    PlaybackController.goToNext()
                }
            }
        }
        player?.addListener(playerListener!!)
        // Actualizamos la UI una vez al registrar el listener
        updateUiFromPlayerState()
    }

    // --- LAS SIGUIENTES FUNCIONES SON SOLO PARA ACTUALIZAR LA INTERFAZ Y SE MANTIENEN ---

    private fun finishWithError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
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
        // Actualizamos la información de la canción cada vez que el estado cambia
        PlayerStatus.currentSong?.let { updateUiWithSongData(it) }
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
        if (binding.tvSongTitle.text == song.title) return // Evita recargar Glide innecesariamente

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

    private suspend fun getCoverUrlForSong(song: Song): String? {
        val session = getSessionData() ?: return null
        // La propia canción sabe cómo construir su URL de portada
        return song.buildCoverArtUrl(session.baseUrl, session.user, session.token, session.salt)
    }

    private suspend fun getSessionData(): SessionData? {
        return try {
            val baseUrl = settingsRepository.serverUrlFlow.first()!!
            val user = settingsRepository.usernameFlow.first()!!
            val token = settingsRepository.tokenFlow.first()!!
            val salt = settingsRepository.saltFlow.first()!!
            SessionData(baseUrl, user, token, salt)
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