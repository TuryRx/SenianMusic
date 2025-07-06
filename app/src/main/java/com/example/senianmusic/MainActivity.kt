package com.example.senianmusic

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.senianmusic.data.local.SettingsRepository

/**
 * Loads [MainFragment].
 */
class MainActivity : Activity() {
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingsRepository = SettingsRepository(applicationContext)

        lifecycleScope.launch {
            val serverUrl = settingsRepository.serverUrlFlow.first() // Obtiene el primer valor
            if (serverUrl.isNullOrBlank()) {
                // No hay URL, lanzar la pantalla de configuración/login
                // startActivity(Intent(this, LoginActivity::class.java))
                // finish()
            } else {
                // La URL ya existe, cargar el contenido principal
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_browse_fragment, MainFragment())
                        .commitNow()
                }
            }
        }
    }
}