package com.example.senianmusic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.senianmusic.data.local.AppDatabase
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.repository.MusicRepository
import com.example.senianmusic.player.MusicPlayer
import com.example.senianmusic.player.PlayerStatus
import com.example.senianmusic.player.PlaybackController
import com.example.senianmusic.ui.MainFragment
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.playback.PlaybackActivity
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    // El ViewModel y otras propiedades siguen igual
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

        // --- LÓGICA DE INICIALIZACIÓN SIMPLIFICADA Y SEGURA ---
        // Ya no comprobamos la sesión aquí. Asumimos que SplashActivity ya lo hizo
        // y que RetrofitClient está listo para ser usado.

        // 1. Inicializamos los componentes que dependen de Retrofit.
        //    Esta llamada es ahora segura.
        val settingsRepository = SettingsRepository(applicationContext)
        val musicRepository = MusicRepository(
            applicationContext,
            AppDatabase.getDatabase(this).songDao(),
            settingsRepository,
            RetrofitClient.getApiService() // ¡Garantizado que no crasheará!
        )
        PlaybackController.initialize(musicRepository, applicationContext)
        Log.d("MainActivity", "Dependencias (Repo, Controller) inicializadas.")

        // 2. Cargamos el fragmento principal
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }

        // 3. Configuramos los listeners de la UI
        setupPlayerBarListeners()
    }

    // --- El resto de tus funciones de MainActivity (onKeyDown, onResume, updatePlayerBar, etc.) ---
    // --- pueden permanecer exactamente iguales. No es necesario cambiarlas.       ---

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            val fragment = supportFragmentManager.findFragmentById(R.id.main_browse_fragment)
            if (fragment is BrowseSupportFragment && playerBarContainer.visibility == View.VISIBLE) {
                val rowsAdapter = fragment.adapter as? ArrayObjectAdapter
                if (rowsAdapter != null && rowsAdapter.size() > 0) {
                    if (fragment.selectedPosition == rowsAdapter.size() - 1) {
                        playerBarContainer.findViewById<ImageButton>(R.id.btn_play_pause)?.requestFocus()
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
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
            PlaybackController.goToNext()
        }

        btnPrev.setOnClickListener {
            PlaybackController.goToPrevious()
        }

        btnExpand.setOnClickListener { openPlaybackActivity() }
        playerBarContainer.setOnClickListener { openPlaybackActivity() }
    }

    private fun openPlaybackActivity() {
        startActivity(Intent(this, PlaybackActivity::class.java))
    }

    private fun showPlayerBarAnimated() {
        val currentFocus = currentFocus
        if (playerBarContainer.visibility == View.VISIBLE) return

        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        slideIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                playerBarContainer.visibility = View.VISIBLE
            }
            override fun onAnimationEnd(animation: Animation?) {
                val mainFragmentView = findViewById<View>(R.id.main_browse_fragment)
                if (currentFocus == null || !mainFragmentView.hasFocus()) {
                    playerBarContainer.findViewById<ImageButton>(R.id.btn_play_pause)?.requestFocus()
                }
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
            if (playerBarContainer.visibility != View.VISIBLE) {
                showPlayerBarAnimated()
            }

            val titleView = playerBarContainer.findViewById<TextView>(R.id.tv_now_playing_title)
            val artistView = playerBarContainer.findViewById<TextView>(R.id.tv_now_playing_artist)
            val coverView = playerBarContainer.findViewById<ImageView>(R.id.iv_now_playing_cover)
            val playPauseBtn = playerBarContainer.findViewById<ImageButton>(R.id.btn_play_pause)
            val progressBar = playerBarContainer.findViewById<ProgressBar>(R.id.now_playing_progress)

            titleView.text = song.title
            artistView.text = song.artist

            val newIconRes = if (PlayerStatus.isPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle
            if (playPauseBtn.tag != newIconRes) {
                playPauseBtn.tag = newIconRes
                playPauseBtn.setImageResource(newIconRes)
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