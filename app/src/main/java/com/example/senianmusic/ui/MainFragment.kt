package com.example.senianmusic.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.player.MusicPlayer
import com.example.senianmusic.player.PlayerStatus
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.model.HomeScreenRow // ¡NUEVO!
import com.example.senianmusic.ui.playback.PlaybackActivity
import com.example.senianmusic.ui.presenter.CardPresenter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log
import com.example.senianmusic.ui.search.SearchActivity


class MainFragment : BrowseSupportFragment() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(requireActivity().applicationContext)
    }

    // El adapter principal que contiene todas las filas (ListRow)
    private lateinit var rowsAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupAdapter()
        setupEventListeners()
        observeViewModelAndLoadData() // Ahora observará las filas
    }

    private fun setupUI() {
        title = "SenianMusic"
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = true

        // --- ¡NUEVO! AÑADIMOS EL LISTENER PARA EL ICONO DE BÚSQUEDA ---
        // Esto automáticamente mostrará el icono de búsqueda en la esquina superior derecha.
        setOnSearchClickedListener {
            // Cuando se haga clic, lanzamos nuestra nueva SearchActivity
            val intent = Intent(requireActivity(), SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAdapter() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, row ->
            // Nos aseguramos de que el clic es en una canción
            if (item is Song) {
                // Comprobación de seguridad: Asegurémonos de que la canción tiene un ID.
                if (item.id.isBlank()) {
                    Log.e("MainFragment", "Se hizo clic en una canción sin ID. No se puede reproducir.")
                    // Opcional: Toast.makeText(requireContext(), "Error: canción no válida", Toast.LENGTH_SHORT).show()
                    return@OnItemViewClickedListener // Salimos del listener
                }

                val listRow = row as ListRow
                val listRowAdapter = listRow.adapter as ArrayObjectAdapter

                val playlist = ArrayList<Song>()
                for (i in 0 until listRowAdapter.size()) {
                    val songItem = listRowAdapter.get(i)
                    if (songItem is Song) { // Añadimos solo las canciones válidas
                        playlist.add(songItem)
                    }
                }
                val currentIndex = playlist.indexOf(item)

                if (currentIndex == -1) {
                    Log.e("MainFragment", "La canción seleccionada no se encontró en la lista de la fila.")
                    return@OnItemViewClickedListener
                }

                // 1. Establecemos el estado global de reproducción.
                PlayerStatus.setPlaylist(playlist, currentIndex)

                // 2. INICIAMOS LA REPRODUCCIÓN (¡LA LÓGICA CLAVE!)
                lifecycleScope.launch {
                    // --- ¡CORRECCIÓN IMPORTANTE! ---
                    // Ya no llamamos a viewModel.getStreamUrlForSong.
                    // Ahora el MusicPlayer y la barra inferior obtienen la URL por su cuenta
                    // usando el ViewModel que ya tienen.
                    // El MusicPlayer.play debe llamarse para INICIAR la reproducción.

                    // Esta lógica la movimos a MainActivity y PlaybackActivity,
                    // pero necesitamos la orden de inicio aquí.
                    val streamUrl = viewModel.getStreamUrlForSong(item) // Asegúrate de que esta función aún exista en el ViewModel
                    if (streamUrl != null) {
                        MusicPlayer.play(requireContext(), streamUrl)
                    } else {
                        Log.e("MainFragment", "No se pudo obtener la URL del stream para ${item.title}")
                    }
                }

                // 3. Lanzamos la actividad de reproducción.
                startActivity(Intent(requireActivity(), PlaybackActivity::class.java))
            }
        }
    }

    private fun observeViewModelAndLoadData() {
        // Observamos el nuevo Flow de filas
        lifecycleScope.launch {
            viewModel.homeScreenRows.collectLatest { homeRows ->
                updateUiWithRows(homeRows)
            }
        }
        // Iniciamos la carga de datos
        viewModel.loadInitialData()
    }

    private fun updateUiWithRows(homeRows: List<HomeScreenRow>) {
        rowsAdapter.clear() // Limpiamos las filas viejas

        val cardPresenter = CardPresenter()

        homeRows.forEach { homeRow ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)

            when (homeRow) {
                is HomeScreenRow.RandomSongsRow -> listRowAdapter.setItems(homeRow.songs, null)
                is HomeScreenRow.RecentlyAddedRow -> listRowAdapter.setItems(homeRow.songs, null)
                is HomeScreenRow.RecentlyPlayedRow -> listRowAdapter.setItems(homeRow.songs, null)
                is HomeScreenRow.RecommendedRow -> listRowAdapter.setItems(homeRow.songs, null)
            }

            val header = HeaderItem(homeRow.id, homeRow.title)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }

        // --- ¡LA SOLUCIÓN MÁGICA ESTÁ AQUÍ! ---
        // Después de añadir todas las filas, comprobamos si el adaptador no está vacío.
        if (rowsAdapter.size() > 0) {
            // Le decimos al BrowseSupportFragment que seleccione la primera fila (índice 0).
            // El segundo parámetro 'true' hace que se desplace suavemente hacia ella.
            setSelectedPosition(0, true)
        }
    }
}