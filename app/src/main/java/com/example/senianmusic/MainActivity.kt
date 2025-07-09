package com.example.senianmusic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
// --- NUEVO --- Importaciones necesarias para la lógica del foco
import android.view.KeyEvent
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
// --- FIN NUEVO ---
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
import com.example.senianmusic.player.MusicPlayer
import com.example.senianmusic.player.PlayerStatus
import com.example.senianmusic.ui.MainFragment
import com.example.senianmusic.ui.login.LoginActivity
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.playback.PlaybackActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.senianmusic.player.PlaybackController // <-- AÑADE ESTA IMPORTACIÓN
import com.example.senianmusic.data.repository.MusicRepository // <-- AÑADE ESTA IMPORTACIÓN
import com.example.senianmusic.data.local.AppDatabase // <-- AÑADE ESTA LÍNEA
import com.example.senianmusic.data.remote.RetrofitClient

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

        // <-- INICIALIZA EL CONTROLADOR AQUÍ
        val musicRepository = MusicRepository(applicationContext, AppDatabase.getDatabase(this).songDao(), settingsRepository, RetrofitClient.getApiService())
        PlaybackController.initialize(musicRepository, applicationContext)

        lifecycleScope.launch {
            // ... resto de tu lógica de onCreate
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

    // --- NUEVO: MÉTODO onKeyDown PARA MANEJAR EL FOCO DEL D-PAD ---
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Solo nos interesa el evento de presionar la flecha "Abajo"
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            val fragment = supportFragmentManager.findFragmentById(R.id.main_browse_fragment)

            // Verificamos que sea nuestro fragmento y que la barra de reproducción esté visible
            if (fragment is BrowseSupportFragment && playerBarContainer.visibility == View.VISIBLE) {

                val rowsAdapter = fragment.adapter as? ArrayObjectAdapter
                if (rowsAdapter != null && rowsAdapter.size() > 0) {

                    // Verificamos si la fila actualmente seleccionada es la ÚLTIMA
                    if (fragment.selectedPosition == rowsAdapter.size() - 1) {

                        // Si es así, movemos el foco al botón de Play/Pause
                        playerBarContainer.findViewById<ImageButton>(R.id.btn_play_pause)?.requestFocus()

                        // Devolvemos 'true' para indicar que hemos manejado el evento.
                        return true
                    }
                }
            }
        }

        // Para cualquier otra tecla o caso, dejamos que el sistema actúe normalmente
        return super.onKeyDown(keyCode, event)
    }
    // --- FIN NUEVO ---


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

        // <-- ¡LA CORRECCIÓN MÁGICA! AHORA LLAMAN AL CONTROLADOR
        btnNext.setOnClickListener {
            PlaybackController.goToNext()
        }

        btnPrev.setOnClickListener {
            PlaybackController.goToPrevious()
        }
        // <-- FIN DE LA CORRECCIÓN

        btnExpand.setOnClickListener { openPlaybackActivity() }
        playerBarContainer.setOnClickListener { openPlaybackActivity() }
    }


    // --- IMPORTANTE: BORRA ESTA FUNCIÓN ---
    // La función playCurrentSongFromBar() ya no es necesaria y causaría más problemas.
    // Borra la función completa.
    /*
    private fun playCurrentSongFromBar() {
        // ... contenido a borrar
    }
    */

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

    // --- MODIFICACIÓN LIGERA PARA MEJORAR LA ROBUSTEZ DEL FOCO ---
    private fun showPlayerBarAnimated() {
        // Guardamos el elemento que tiene el foco actualmente en el fragmento principal.
        val currentFocus = currentFocus

        if (playerBarContainer.visibility == View.VISIBLE) return

        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        slideIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                playerBarContainer.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                // Si el foco sigue en el fragmento principal (y no se ha movido por otra razón),
                // no forzamos el foco a la barra, dejamos que onKeyDown haga su trabajo.
                // Esto evita un comportamiento errático si el usuario es muy rápido.
                // Sin embargo, si el foco se perdió, lo ponemos en play/pause como respaldo.
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
            // Ya no llamamos a showPlayerBarAnimated() directamente desde aquí.
            // Dejaremos que onKeyDown se encargue del foco al bajar.
            // Solo nos aseguramos de que la barra sea visible si no lo está.
            if (playerBarContainer.visibility != View.VISIBLE) {
                showPlayerBarAnimated()
            }

            // El resto de la lógica de actualización de la UI
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
                // He simplificado la animación del botón para evitar posibles conflictos
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