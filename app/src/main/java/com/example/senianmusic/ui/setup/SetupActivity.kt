package com.example.senianmusic.ui.setup

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity // Usamos FragmentActivity para compatibilidad con TV
import com.example.senianmusic.databinding.ActivitySetupBinding // Esto usa ViewBinding

class SetupActivity : FragmentActivity() {

    private lateinit var binding: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar la vista usando ViewBinding
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el botón
        binding.btnConnect.setOnClickListener {
            val url = binding.etServerUrl.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (url.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                // AQUÍ es donde guardaremos los datos y haremos el login más adelante
                Toast.makeText(this, "Conectando a $url...", Toast.LENGTH_SHORT).show()

                // TODO: Implementar DataStore y Retrofit para guardar y conectar

            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_LONG).show()
            }
        }
    }
}