package com.example.senianmusic.ui // O el paquete correcto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.MainActivity
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No necesitamos un layout, esta actividad es solo para decidir
        // setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            val settingsRepository = SettingsRepository(applicationContext)
            val serverUrl = settingsRepository.serverUrlFlow.first()

            if (serverUrl.isNullOrBlank()) {
                // No hay sesión, vamos al Login
                Log.d("SplashActivity", "No se encontró sesión. Navegando a LoginActivity.")
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Sesión encontrada. INICIALIZAMOS TODO AQUÍ antes de ir a MainActivity
                Log.d("SplashActivity", "Sesión encontrada. URL: $serverUrl. Inicializando dependencias...")

                // Inicializamos RetrofitClient aquí, una única vez al arrancar
                RetrofitClient.initialize(serverUrl)

                Log.d("SplashActivity", "Dependencias listas. Navegando a MainActivity.")
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
            }

            // Finalizamos SplashActivity para que el usuario no pueda volver a ella
            finish()
        }
    }
}