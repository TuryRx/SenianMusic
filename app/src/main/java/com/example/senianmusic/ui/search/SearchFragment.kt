package com.example.senianmusic.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.data.local.AppDatabase
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import com.example.senianmusic.player.MusicPlayer
import com.example.senianmusic.player.PlayerStatus
import com.example.senianmusic.ui.playback.PlaybackActivity
import com.example.senianmusic.ui.presenter.CardPresenter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.os.Parcelable

class SearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    // --- CAMBIO EN LA CREACIÓN DEL VIEWMODEL ---
    private val viewModel: SearchViewModel by viewModels {
        // Obtenemos el repositorio primero...
        val repository = MusicRepository(
            requireActivity().applicationContext,
            AppDatabase.getDatabase(requireContext()).songDao(),
            SettingsRepository(requireContext()),
            RetrofitClient.getApiService() // Ahora podemos llamar a getApiService de forma segura
        )
        // ...y se lo pasamos al Factory.
        SearchViewModelFactory(repository)
    }

    private lateinit var rowsAdapter: ArrayObjectAdapter
    private val cardPresenter = CardPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)

        // --- ¡NUEVO! INICIALIZAR RETROFIT ANTES DE USARLO ---
        // Nos aseguramos de que Retrofit esté inicializado antes de hacer cualquier búsqueda.
        lifecycleScope.launch {
            val settingsRepository = SettingsRepository(requireContext())
            val baseUrl = settingsRepository.serverUrlFlow.first()
            if (!baseUrl.isNullOrBlank()) {
                RetrofitClient.initialize(baseUrl)
            } else {
                Log.e("SearchFragment", "No se puede buscar porque la URL del servidor no está configurada.")
                // Opcional: Mostrar un Toast y cerrar la actividad si no hay URL
                // Toast.makeText(requireContext(), "Error: URL del servidor no encontrada", Toast.LENGTH_LONG).show()
                activity?.finish()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        setupEventListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.searchResults.collectLatest { songs ->
                displayResults(songs)
            }
        }
    }

    private fun displayResults(songs: List<Song>) {
        rowsAdapter.clear()
        if (songs.isNotEmpty()) {
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            listRowAdapter.addAll(0, songs)
            val header = HeaderItem("Resultados de la búsqueda")
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
        // Si no hay resultados, el adapter simplemente queda vacío, mostrando un mensaje
        // que el SearchSupportFragment maneja por defecto ("No results found.").
    }

    override fun onQueryTextChange(newQuery: String?): Boolean {
        // Devolvemos true para que la UI se actualice, pero no ejecutamos la búsqueda aquí
        // para no sobrecargar el servidor con cada letra tecleada.
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.d("SearchFragment", "Búsqueda enviada: $query")
        if (!query.isNullOrBlank()) {
            // Le decimos al ViewModel que ejecute la búsqueda
            viewModel.executeSearch(query)
        } else {
            // Si la búsqueda está en blanco, limpiamos los resultados
            rowsAdapter.clear()
        }
        return true
    }

    override fun getResultsAdapter(): ObjectAdapter {
        // El adaptador que contendrá las filas de resultados
        return rowsAdapter
    }

    private fun setupEventListeners() {
        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Song) {
                // 1. Obtenemos todos los resultados actuales para crear la masterPlaylist
                val playlist = ArrayList<Parcelable>()
                (rowsAdapter.get(0) as? ListRow)?.adapter?.let { adapter ->
                    for (i in 0 until adapter.size()) {
                        if (adapter.get(i) is Song) {
                            playlist.add(adapter.get(i) as Song)
                        }
                    }
                }

                if (playlist.isEmpty()) {
                    // Como fallback, si algo sale mal, creamos una playlist con la canción actual
                    playlist.add(item)
                }

                // 2. Encontramos el índice de la canción clickeada
                val startIndex = playlist.indexOf(item)

                // 3. Lanzamos PlaybackActivity con la información
                val intent = Intent(requireActivity(), PlaybackActivity::class.java).apply {
                    action = PlaybackActivity.ACTION_START_PLAYBACK
                    putParcelableArrayListExtra(PlaybackActivity.EXTRA_MASTER_PLAYLIST, playlist)
                    putExtra(PlaybackActivity.EXTRA_START_INDEX, if (startIndex != -1) startIndex else 0)
                }
                startActivity(intent)
            }
        }
    }
}