package com.example.senianmusic

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.data.local.SettingsRepository // Asumimos que esta clase existe
import com.example.senianmusic.ui.login.LoginActivity // Asumimos que esta clase existe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Actividad principal que decide si mostrar la pantalla de login/configuración
 * o la pantalla principal de contenido.
 */
class MainActivity : FragmentActivity() { // <-- ¡CORREGIDO! Hereda de FragmentActivity

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa el repositorio de configuración
        // Asegúrate de que SettingsRepository exista y pueda ser instanciado así.
        settingsRepository = SettingsRepository(applicationContext)

        // Usa una corrutina para comprobar si la URL del servidor está guardada
        lifecycleScope.launch {
            val serverUrl = settingsRepository.serverUrlFlow.first()

            if (serverUrl.isNullOrBlank()) {
                // No hay URL guardada, redirigir al usuario a la pantalla de Login.
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish() // Cierra MainActivity para que el usuario no pueda volver a ella con el botón "atrás"
            } else {
                // Ya tenemos una URL, cargamos el fragmento principal.
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_browse_fragment, MainFragment())
                        .commitNow()
                }
            }
        }
    }
}