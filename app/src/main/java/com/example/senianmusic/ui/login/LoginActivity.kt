package com.example.senianmusic.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.MainActivity
import com.example.senianmusic.data.local.SettingsRepository // Asegúrate de que esta clase exista
import com.example.senianmusic.data.remote.RetrofitClient // Asegúrate de que esta clase exista
import com.example.senianmusic.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Estas dos clases DEBEN existir en tu proyecto para que esto compile:
        settingsRepository = SettingsRepository(applicationContext)
        // RetrofitClient.initialize(...)

        binding.connectButton.setOnClickListener {
            val url = binding.urlEditText.text.toString().trim()
            val user = binding.userEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (url.isNotEmpty() && user.isNotEmpty()) {
                val finalUrl = if (url.endsWith("/")) url else "$url/"

                // Asegúrate de que RetrofitClient.initialize exista y funcione
                RetrofitClient.initialize(finalUrl)

                lifecycleScope.launch {
                    settingsRepository.saveServerUrl(finalUrl)
                    // TODO: Aquí también guardarías el usuario/token y harías la llamada de login
                }

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}