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
import com.example.senianmusic.data.remote.NavidromeApiService
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.remote.model.SubsonicResponse // Asegúrate de importar tu clase de respuesta
import com.example.senianmusic.databinding.ActivityLoginBinding
import com.example.senianmusic.util.toMD5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginActivity : FragmentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var binding: ActivityLoginBinding

    // --- NUEVO: Añadimos una función de ayuda para la llamada a la API de ping ---
    // Esto hace el código más limpio. Esta función debe estar en NavidromeApiService
    /*
    @GET("rest/ping.view")
    suspend fun ping(
        @Query("u") user: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        // ... otros parámetros ...
    ): Response<SubsonicResponse>
    */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(applicationContext)

        // Lógica para cambiar entre formularios
        binding.serverLoginButton.setOnClickListener { showServerLoginForm() }
        binding.emailLoginButton.setOnClickListener { showEmailLoginForm() }
        binding.serverLoginButton.requestFocus()

        // --- LÓGICA DE CONEXIÓN MODIFICADA ---
        binding.connectButton.setOnClickListener {
            val url = binding.urlEditText.text.toString().trim()
            val user = binding.userEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (url.isEmpty() || user.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostramos el progreso
            showLoading(true)

            // Iniciamos la corrutina para el trabajo de red y de disco
            lifecycleScope.launch {
                val loginSuccessful = performLogin(url, user, password)

                // Una vez que la corrutina termina, volvemos al hilo principal para la navegación
                if (loginSuccessful) {
                    Log.d("LoginActivity", "Login exitoso. Navegando a MainActivity.")
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish() // Ahora es seguro llamar a finish()
                } else {
                    Log.d("LoginActivity", "Login fallido. Habilitando UI de nuevo.")
                    showLoading(false)
                }
            }
        }

        binding.loginWithEmailButton.setOnClickListener {
            Toast.makeText(this, "Esta función aún no está implementada", Toast.LENGTH_SHORT).show()
        }
    }

    // --- NUEVA FUNCIÓN SUSPEND PARA HACER TODO EL TRABAJO PESADO ---
    private suspend fun performLogin(url: String, user: String, password: String): Boolean {
        // Usamos withContext para asegurarnos de que todo esto se ejecute en un hilo de fondo
        return withContext(Dispatchers.IO) {
            try {
                var tempUrl = url
                if (!tempUrl.startsWith("http://") && !tempUrl.startsWith("https://")) {
                    tempUrl = "http://$tempUrl"
                }

                var finalUrl = tempUrl
                if (finalUrl.contains("127.0.0.1") || finalUrl.contains("localhost")) {
                    finalUrl = finalUrl.replace("127.0.0.1", "10.0.2.2").replace("localhost", "10.0.2.2")
                }
                finalUrl = if (finalUrl.endsWith("/")) finalUrl else "$finalUrl/"

                RetrofitClient.initialize(finalUrl)

                val salt = "senian_salt"
                val token = (password + salt).toMD5()

                // Usamos la API de ping, que es mucho más ligera que getArtists
                val response = RetrofitClient.getApiService().ping(user, token, salt)

                if (response.isSuccessful && response.body()?.subsonicResponse?.status == "ok") {
                    Log.d("LoginActivity", "API Ping exitoso. Guardando sesión...")
                    settingsRepository.saveLoginSession(finalUrl, user, token, salt)
                    Log.d("LoginActivity", "¡Sesión guardada exitosamente en el hilo de IO!")

                    // Mostramos el Toast desde el hilo principal
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "¡Conexión exitosa!", Toast.LENGTH_SHORT).show()
                    }
                    true // Devolvemos 'true' para indicar éxito
                } else {
                    Log.e("LoginActivity", "Autenticación fallida. Código: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Usuario o contraseña incorrectos.", Toast.LENGTH_LONG).show()
                    }
                    false // Devolvemos 'false' para indicar fallo
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error de conexión o de red", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
                false // Devolvemos 'false' para indicar fallo
            }
        }
    }

    private fun showServerLoginForm() {
        binding.serverLoginForm.visibility = View.VISIBLE
        binding.emailLoginForm.visibility = View.GONE
    }

    private fun showEmailLoginForm() {
        binding.serverLoginForm.visibility = View.GONE
        binding.emailLoginForm.visibility = View.VISIBLE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.connectButton.isEnabled = !isLoading
        binding.serverLoginButton.isEnabled = !isLoading
        binding.emailLoginButton.isEnabled = !isLoading
    }
}