package com.example.senianmusic.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log // <-- IMPORT AÑADIDO
import android.view.View // <-- IMPORT AÑADIDO
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.MainActivity
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.databinding.ActivityLoginBinding
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
            val url = binding.urlEditText.text.toString().trim()
            val user = binding.userEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (url.isNotEmpty() && user.isNotEmpty() && password.isNotEmpty()) {
                // Inicia feedback visual
                binding.connectButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE

                val finalUrl = if (url.endsWith("/")) url else "$url/"
                RetrofitClient.initialize(finalUrl)

                lifecycleScope.launch {
                    try {
                        val fakeToken = "faketoken"
                        val fakeSalt = "fakesalt"
                        Log.d("LoginActivity", "Intentando hacer PING al servidor...")
                        val response = RetrofitClient.getApiService().ping(user, fakeToken, fakeSalt)

                        if (response.isSuccessful) {
                            Log.d("LoginActivity", "Ping exitoso!")
                            Toast.makeText(this@LoginActivity, "¡Conexión exitosa!", Toast.LENGTH_SHORT).show()
                            settingsRepository.saveServerUrl(finalUrl)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.e("LoginActivity", "Ping fallido! Código: ${response.code()}")
                            Toast.makeText(this@LoginActivity, "Error del servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Excepción al hacer ping", e) // Pasamos la excepción completa al log
                        Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        // Restaura la UI
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