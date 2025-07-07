package com.example.senianmusic.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.MainActivity
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.databinding.ActivityLoginBinding
import com.example.senianmusic.util.toMD5
import kotlinx.coroutines.launch

class LoginActivity : FragmentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(applicationContext)

        binding.connectButton.setOnClickListener {
            var url = binding.urlEditText.text.toString().trim() // Cambiado a 'var'
            val user = binding.userEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (url.isNotEmpty() && user.isNotEmpty() && password.isNotEmpty()) {
                binding.connectButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE

                // 1. Normalizamos la URL para asegurar que tenga http/https
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://$url" // Por defecto, intentamos con http
                }

                // 2. Reemplazamos la IP si estamos en el emulador
                var finalUrl = url
                if (finalUrl.contains("127.0.0.1") || finalUrl.contains("localhost")) {
                    finalUrl = finalUrl.replace("127.0.0.1", "10.0.2.2").replace("localhost", "10.0.2.2")
                    Log.d("LoginActivity", "URL de localhost reemplazada por: $finalUrl")
                }
                finalUrl = if (finalUrl.endsWith("/")) finalUrl else "$finalUrl/"

                RetrofitClient.initialize(finalUrl)

                lifecycleScope.launch {
                    try {
                        val salt = "senian_salt"
                        val token = (password + salt).toMD5()
                        val response = RetrofitClient.getApiService().getArtists(user, token, salt)

                        if (response.isSuccessful) {
                            Log.d("LoginActivity", "¡Autenticación exitosa!")
                            Toast.makeText(this@LoginActivity, "¡Conexión exitosa!", Toast.LENGTH_SHORT).show()

                            // Guardamos toda la sesión
                            settingsRepository.saveLoginSession(finalUrl, user, token, salt)

                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.e("LoginActivity", "Autenticación fallida. Código: ${response.code()}")
                            Toast.makeText(this@LoginActivity, "Usuario o contraseña incorrectos.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Error de conexión o de red", e)
                        Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        binding.connectButton.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}