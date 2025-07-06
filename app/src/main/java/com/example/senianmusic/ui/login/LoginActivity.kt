package com.example.senianmusic.ui.login
import androidx.appcompat.app.AppCompatActivity
import com.example.senianmusic.data.local.SettingsRepository // Asegúrate de tener este import
import com.example.senianmusic.databinding.ActivityLoginBinding // Y el de ViewBinding


class LoginActivity : AppCompatActivity() {
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

            if (url.isNotEmpty() && user.isNotEmpty()) {
                // --- AQUÍ OCURRE LA MAGIA ---

                // 1. INICIALIZAMOS Retrofit con la URL que acaba de dar el usuario.
                // Asegúrate de que la URL termine en "/"
                val finalUrl = if (url.endsWith("/")) url else "$url/"
                RetrofitClient.initialize(finalUrl)

                // 2. Guardamos la URL para no volver a pedirla.
                lifecycleScope.launch {
                    settingsRepository.saveServerUrl(finalUrl)
                    // También guardarías el usuario y contraseña/token aquí
                }

                // 3. Vamos a la pantalla principal de la app.
                startActivity(Intent(this, MainActivity::class.java))
                finish() // Cierra la pantalla de login
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}