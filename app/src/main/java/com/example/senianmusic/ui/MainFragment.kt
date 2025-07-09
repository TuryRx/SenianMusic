package com.example.senianmusic.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.ui.album.AlbumDetailActivity
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.model.HomeScreenRow
import com.example.senianmusic.ui.playback.PlaybackActivity
import com.example.senianmusic.ui.presenter.UniversalCardPresenter
import com.example.senianmusic.ui.search.SearchActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainFragment : BrowseSupportFragment() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(requireActivity().applicationContext)
    }

    private lateinit var rowsAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupAdapter()
        setupEventListeners()
        observeViewModelAndLoadData()
    }

    private fun setupUI() {
        title = "SenianMusic"
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = true
        setOnSearchClickedListener {
            startActivity(Intent(requireActivity(), SearchActivity::class.java))
        }
    }

    private fun setupAdapter() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
    }

    // --- setupEventListeners CON LA LÓGICA RESTAURADA ---
    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, row ->
            val clickedRow = row as? ListRow ?: return@OnItemViewClickedListener

            when (item) {
                is Song -> {
                    // Si es una canción, siempre iniciamos la reproducción de la lista completa
                    Log.d("MainFragment", "Canción '${item.title}' clickeada. Iniciando lista maestra...")
                    startMasterPlaylistPlayback(item, clickedRow)
                }
                is Album -> {
                    // Si es un álbum, decidimos qué hacer
                    if (item.songCount == 1) {
                        // Si es un "single", iniciamos la reproducción de la lista completa
                        Log.d("MainFragment", "Single '${item.name}' clickeado. Iniciando lista maestra...")
                        startMasterPlaylistPlayback(item, clickedRow)
                    } else {
                        // Si es un álbum completo, navegamos a los detalles
                        Log.d("MainFragment", "Álbum '${item.name}' clickeado. Navegando a detalles...")
                        navigateToAlbumDetail(item)
                    }
                }
            }
        }
    }

    /**
     * NUEVA FUNCIÓN DE AYUDA para no repetir código.
     * Crea la lista maestra a partir de la fila y lanza PlaybackActivity.
     */
    private fun startMasterPlaylistPlayback(item: Parcelable, row: ListRow) {
        val listRowAdapter = row.adapter

        // 1. Crear la lista maestra de Parcelables
        val masterPlaylist = ArrayList<Parcelable>()
        for (i in 0 until listRowAdapter.size()) {
            (listRowAdapter.get(i) as? Parcelable)?.let {
                masterPlaylist.add(it)
            }
        }

        if (masterPlaylist.isEmpty()) return

        // 2. Encontrar el índice del elemento clickeado
        val startIndex = masterPlaylist.indexOf(item)

        // 3. Lanzar PlaybackActivity con toda la información
        val intent = Intent(requireActivity(), PlaybackActivity::class.java).apply {
            action = PlaybackActivity.ACTION_START_PLAYBACK
            putParcelableArrayListExtra(PlaybackActivity.EXTRA_MASTER_PLAYLIST, masterPlaylist)
            putExtra(PlaybackActivity.EXTRA_START_INDEX, if (startIndex != -1) startIndex else 0)
        }
        startActivity(intent)
    }

    /**
     * RESTAURAMOS ESTA FUNCIÓN para navegar a los detalles del álbum.
     */
    private fun navigateToAlbumDetail(album: Album) {
        val intent = Intent(requireActivity(), AlbumDetailActivity::class.java).apply {
            putExtra(AlbumDetailActivity.ALBUM_ID, album.id)
            putExtra(AlbumDetailActivity.ALBUM_NAME, album.name)
        }
        startActivity(intent)
    }

    private fun observeViewModelAndLoadData() {
        lifecycleScope.launch {
            viewModel.homeScreenRows.collectLatest { homeRows ->
                updateUiWithRows(homeRows)
            }
        }
        viewModel.loadInitialData()
    }

    private fun updateUiWithRows(homeRows: List<HomeScreenRow>) {
        rowsAdapter.clear()
        val cardPresenter = UniversalCardPresenter()
        homeRows.forEach { homeRow ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            when (homeRow) {
                is HomeScreenRow.RandomSongsRow -> listRowAdapter.setItems(homeRow.songs, null)
                is HomeScreenRow.RecentlyAddedRow -> listRowAdapter.setItems(homeRow.albums, null)
                is HomeScreenRow.RecentlyPlayedRow -> listRowAdapter.setItems(homeRow.albums, null)
                is HomeScreenRow.RecommendedRow -> listRowAdapter.setItems(homeRow.songs, null)
            }
            val header = HeaderItem(homeRow.id, homeRow.title)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
        if (rowsAdapter.size() > 0) {
            setSelectedPosition(0, true)
        }
    }
}