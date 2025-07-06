package com.example.senianmusic.ui.main

// TODOS LOS IMPORTS VAN AQUÍ ARRIBA
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.senianmusic.R
import com.example.senianmusic.data.remote.model.Song // Importa el modelo Song

// COMIENZA LA DECLARACIÓN DE LA CLASE
class MainFragment : Fragment(R.layout.fragment_main) {

    // LA DECLARACIÓN DEL VIEWMODEL VA AQUÍ DENTRO
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(requireContext().applicationContext)
    }

    // EL RESTO DEL CÓDIGO DE LA CLASE VA AQUÍ DENTRO
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ... tu código ...

        val someSong = getSongFromSomewhere()
        // por ejemplo, un botón imaginario
        // view.findViewById<Button>(R.id.download_button).setOnClickListener {
        //     viewModel.onDownloadSongClicked(someSong)
        // }
    }

    private fun getSongFromSomewhere(): Song {
        return Song(
            id = "song-123",
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            duration = 355
        )
    }
} // LA CLASE TERMINA AQUÍ