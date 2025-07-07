package com.example.senianmusic

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.player.MusicPlayer // <-- IMPORTAMOS NUESTRO REPRODUCTOR
import com.example.senianmusic.ui.MainFragment
import com.example.senianmusic.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingsRepository = SettingsRepository(applicationContext)

        lifecycleScope.launch {
            // Comprobamos si el usuario ya ha iniciado sesión
            val serverUrl = settingsRepository.serverUrlFlow.first()

            if (serverUrl.isNullOrBlank()) {
                // No hay sesión, vamos al Login
                navigateToLogin()
            } else {
                // Hay sesión, cargamos el fragmento principal
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_browse_fragment, MainFragment())
                        .commitNow()
                }
            }
        }
    }

    /**
     * Sobrescribimos onDestroy para liberar los recursos del reproductor
     * cuando esta actividad principal se destruye.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Liberamos el ExoPlayer para evitar fugas de memoria y consumo de batería.
        MusicPlayer.release()
    }

    private fun navigateToLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish() // Cierra MainActivity
    }
}