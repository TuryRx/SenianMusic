package com.example.senianmusic

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.data.remote.model.Song // Asegúrate de que este modelo exista
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.presenter.CardPresenter // Crearemos este presentador
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.widget.Toast


class MainFragment : BrowseSupportFragment() {

    // 1. Conecta con el ViewModel usando la Factory que creamos
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(requireActivity().applicationContext)
    }

    private lateinit var rowsAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupEventListeners()

        // 2. Inicializa el adapter principal que contendrá todas las filas
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter

        // 3. Observa los datos del ViewModel
        observeViewModelData()

        // 4. Pide al ViewModel que cargue los datos iniciales
        viewModel.loadInitialData()
    }

    private fun setupUI() {
        title = getString(R.string.browse_title) // O "SenianMusic"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = requireContext().getColor(R.color.fastlane_background)
        searchAffordanceColor = requireContext().getColor(R.color.search_opaque)
    }

    private fun observeViewModelData() {
        // Usamos lifecycleScope para observar los Flows de forma segura
        viewLifecycleOwner.lifecycleScope.launch {
            // Observa la lista de canciones (esto es un ejemplo, podrías tener artistas, álbumes, etc.)
            viewModel.songs.collectLatest { songList ->
                if (songList.isNotEmpty()) {
                    updateRowsWithSongs(songList)
                }
            }
        }
    }

    private fun updateRowsWithSongs(songs: List<Song>) {
        // Limpia las filas anteriores para no duplicar datos
        rowsAdapter.clear()

        // Crea un presentador para las tarjetas de las canciones
        val songCardPresenter = CardPresenter()

        // El adapter para la fila de canciones
        val songListRowAdapter = ArrayObjectAdapter(songCardPresenter)
        songListRowAdapter.setItems(songs, null)

        // El cabezal para la fila
        val header = HeaderItem(0, "Todas las Canciones")

        // Añade la nueva fila al adapter principal
        rowsAdapter.add(ListRow(header, songListRowAdapter))
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is Song) {
                // Lógica para cuando se hace clic en una canción (ej. reproducir)
                Log.d("MainFragment", "Clicked on song: ${item.title}")
                Toast.makeText(requireContext(), "Reproduciendo: ${item.title}", Toast.LENGTH_SHORT).show()
                // Aquí llamarías a viewModel.playSong(item)
            }
        }

        // Podrías añadir un listener para descargar al mantener pulsado
        // (esto requiere un poco más de configuración en el Presenter)
    }
}