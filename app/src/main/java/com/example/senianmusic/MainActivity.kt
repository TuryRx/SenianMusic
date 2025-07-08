package com.example.senianmusic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.player.MusicPlayer
import com.example.senianmusic.player.PlayerStatus
import com.example.senianmusic.ui.MainFragment
import com.example.senianmusic.ui.login.LoginActivity
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.playback.PlaybackActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(applicationContext) }
    private lateinit var playerBarContainer: ConstraintLayout
    private val playerStatusListener: () -> Unit = { updatePlayerBar() }

    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressUpdater: Runnable = object : Runnable {
        override fun run() {
            if (PlayerStatus.isPlaying) {
                PlayerStatus.currentPosition = MusicPlayer.getInstance(this@MainActivity).currentPosition
                val progressBar = playerBarContainer.findViewById<ProgressBar>(R.id.now_playing_progress)
                progressBar.progress = PlayerStatus.currentPosition.toInt()
                progressHandler.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerBarContainer = findViewById(R.id.bottom_player_bar_container)
        settingsRepository = SettingsRepository(applicationContext)
        lifecycleScope.launch {
            val serverUrl = settingsRepository.serverUrlFlow.first()
            if (serverUrl.isNullOrBlank()) {
                navigateToLogin()
            } else {
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_browse_fragment, MainFragment())
                        .commitNow()
                }
                setupPlayerBarListeners()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PlayerStatus.addListener(playerStatusListener)
        updatePlayerBar()
    }

    override fun onPause() {
        super.onPause()
        PlayerStatus.removeListener(playerStatusListener)
        progressHandler.removeCallbacks(progressUpdater)
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayer.release()
    }

    private fun navigateToLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupPlayerBarListeners() {
        val btnPlayPause = playerBarContainer.findViewById<ImageButton>(R.id.btn_play_pause)
        val btnNext = playerBarContainer.findViewById<ImageButton>(R.id.btn_next)
        val btnPrev = playerBarContainer.findViewById<ImageButton>(R.id.btn_previous)
        val btnExpand = playerBarContainer.findViewById<ImageView>(R.id.iv_expand_arrow)

        btnPlayPause.setOnClickListener {
            val willBePlaying = !PlayerStatus.isPlaying
            PlayerStatus.updateIsPlaying(willBePlaying)
            if (willBePlaying) MusicPlayer.resume() else MusicPlayer.pause()
        }

        btnNext.setOnClickListener {
            PlayerStatus.playNext()
            playCurrentSongFromBar()
        }

        btnPrev.setOnClickListener {
            PlayerStatus.playPrevious()
            playCurrentSongFromBar()
        }

        btnExpand.setOnClickListener { openPlaybackActivity() }
        playerBarContainer.setOnClickListener { openPlaybackActivity() }
    }

    private fun playCurrentSongFromBar() {
        val song = PlayerStatus.currentSong ?: return
        lifecycleScope.launch {
            val streamUrl = viewModel.getStreamUrlForSong(song)
            if (streamUrl != null) {
                MusicPlayer.play(this@MainActivity, streamUrl)
            } else {
                Toast.makeText(this@MainActivity, "No se pudo obtener la URL", Toast.LENGTH_SHORT).show()
                PlayerStatus.updateIsPlaying(false)
            }
        }
    }

    private fun openPlaybackActivity() {
        startActivity(Intent(this, PlaybackActivity::class.java))
    }

    private fun showPlayerBarAnimated() {
        // Si la barra ya está visible, no hacemos nada.
        if (playerBarContainer.visibility == View.VISIBLE) return

        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        slideIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Hacemos visible la barra JUSTO ANTES de que empiece la animación
                playerBarContainer.visibility = View.VISIBLE
            }
            override fun onAnimationEnd(animation: Animation?) {
                // --- ¡LA MAGIA OCURRE AQUÍ! ---
                // Cuando la animación termina, le pedimos al botón de Play/Pause que tome el foco.
                val playPauseButton = playerBarContainer.findViewById<ImageButton>(R.id.btn_play_pause)
                playPauseButton.requestFocus()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        playerBarContainer.startAnimation(slideIn)
    }

    private fun hidePlayerBarAnimated() {
        if (playerBarContainer.visibility == View.GONE) return
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_bottom)
        slideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                playerBarContainer.visibility = View.GONE
            }
        })
        playerBarContainer.startAnimation(slideOut)
    }

    private fun updatePlayerBar() {
        val song = PlayerStatus.currentSong
        if (song != null) {
            showPlayerBarAnimated()
            val songInfoLayout = playerBarContainer.findViewById<LinearLayout>(R.id.ll_song_info)
            val titleView = songInfoLayout.findViewById<TextView>(R.id.tv_now_playing_title)
            val artistView = songInfoLayout.findViewById<TextView>(R.id.tv_now_playing_artist)
            val coverView = playerBarContainer.findViewById<ImageView>(R.id.iv_now_playing_cover)
            val playPauseBtn = playerBarContainer.findViewById<ImageButton>(R.id.btn_play_pause)
            val progressBar = playerBarContainer.findViewById<ProgressBar>(R.id.now_playing_progress)

            titleView.text = song.title
            artistView.text = song.artist

            val newIconRes = if (PlayerStatus.isPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle
            if (playPauseBtn.tag != newIconRes) {
                playPauseBtn.tag = newIconRes
                val fadeOut = AlphaAnimation(1.0f, 0.0f).apply { duration = 150 }
                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        playPauseBtn.setImageResource(newIconRes)
                        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply { duration = 150 }
                        playPauseBtn.startAnimation(fadeIn)
                    }
                })
                playPauseBtn.startAnimation(fadeOut)
            }

            if (PlayerStatus.totalDuration > 0) {
                progressBar.max = PlayerStatus.totalDuration.toInt()
                progressBar.progress = PlayerStatus.currentPosition.toInt()
            } else {
                progressBar.progress = 0
            }

            progressHandler.removeCallbacks(progressUpdater)
            if (PlayerStatus.isPlaying) {
                progressHandler.post(progressUpdater)
            }

            lifecycleScope.launch {
                val coverUrl = viewModel.getCoverUrlForSong(song)
                Glide.with(this@MainActivity)
                    .load(coverUrl)
                    .placeholder(R.drawable.movie)
                    .into(coverView)
            }
        } else {
            hidePlayerBarAnimated()
            progressHandler.removeCallbacks(progressUpdater)
        }
    }
}