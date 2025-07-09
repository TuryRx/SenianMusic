package com.example.senianmusic.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.R
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.ui.album.AlbumDetailActivity
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.model.HomeScreenRow
import com.example.senianmusic.ui.playback.PlaybackActivity
import com.example.senianmusic.ui.presenter.ActionPresenter
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
        brandColor = ContextCompat.getColor(requireContext(), R.color.brand_color)

        setOnSearchClickedListener {
            startActivity(Intent(requireActivity(), SearchActivity::class.java))
        }
    }

    private fun setupAdapter() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, row ->
            val clickedRow = row as? ListRow

            // --- LÓGICA DE CLIC ACTUALIZADA ---
            when (item) {
                // 1. Manejamos los clics en los botones de acción
                is String -> {
                    when (item) {
                        "Ajustes" -> {
                            startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                        }
                        // Aquí puedes añadir la lógica para los otros botones
                        "Random" -> Toast.makeText(context, "Función 'Random' no implementada", Toast.LENGTH_SHORT).show()
                        "Favoritos" -> Toast.makeText(context, "Función 'Favoritos' no implementada", Toast.LENGTH_SHORT).show()
                        "Lista" -> Toast.makeText(context, "Función 'Lista' no implementada", Toast.LENGTH_SHORT).show()
                    }
                }
                // 2. Manejamos el contenido musical
                is Song -> {
                    if (clickedRow != null) startMasterPlaylistPlayback(item, clickedRow)
                }
                is Album -> {
                    if (clickedRow != null) {
                        if (item.songCount == 1) startMasterPlaylistPlayback(item, clickedRow)
                        else navigateToAlbumDetail(item)
                    }
                }
            }
        }
    }

    private fun observeViewModelAndLoadData() {
        lifecycleScope.launch {
            viewModel.homeScreenRows.collectLatest { homeRows ->
                updateUiWithRows(homeRows)
            }
        }
        viewModel.loadInitialData()
    }

    // --- FUNCIÓN DE ACTUALIZACIÓN DE UI MODIFICADA ---
    private fun updateUiWithRows(homeRows: List<HomeScreenRow>) {
        rowsAdapter.clear()

        // 1. Añadimos primero la fila de acciones rápidas
        addQuickActionRow()

        // 2. Luego, añadimos las filas de música
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

    // --- NUEVA FUNCIÓN PARA LA FILA DE ACCIONES RÁPIDAS ---
    private fun addQuickActionRow() {
        // No le ponemos título al HeaderItem para que solo se vean los botones
        val header = HeaderItem(-1L, "")
        val listRowAdapter = ArrayObjectAdapter(ActionPresenter())

        // Añadimos los 4 botones
        listRowAdapter.add("Random")
        listRowAdapter.add("Favoritos")
        listRowAdapter.add("Lista")
        listRowAdapter.add("Ajustes")

        rowsAdapter.add(ListRow(header, listRowAdapter))
    }

    // --- ESTAS FUNCIONES SE MANTIENEN IGUAL ---
    private fun findRowForSong(song: Song): ListRow? {
        for (i in 0 until rowsAdapter.size()) {
            val row = rowsAdapter.get(i) as? ListRow
            val adapter = row?.adapter
            if (adapter != null) {
                for (j in 0 until adapter.size()) {
                    if (adapter.get(j) == song) return row
                }
            }
        }
        return null
    }

    private fun findRowForAlbum(album: Album): ListRow? {
        for (i in 0 until rowsAdapter.size()) {
            val row = rowsAdapter.get(i) as? ListRow
            val adapter = row?.adapter
            if (adapter != null) {
                for (j in 0 until adapter.size()) {
                    if (adapter.get(j) == album) return row
                }
            }
        }
        return null
    }

    private fun startMasterPlaylistPlayback(item: Parcelable, row: ListRow) {
        val listRowAdapter = row.adapter
        val masterPlaylist = ArrayList<Parcelable>()
        for (i in 0 until listRowAdapter.size()) {
            (listRowAdapter.get(i) as? Parcelable)?.let {
                masterPlaylist.add(it)
            }
        }
        if (masterPlaylist.isEmpty()) return
        val startIndex = masterPlaylist.indexOf(item)

        val intent = Intent(requireActivity(), PlaybackActivity::class.java).apply {
            action = PlaybackActivity.ACTION_START_PLAYBACK
            putParcelableArrayListExtra(PlaybackActivity.EXTRA_MASTER_PLAYLIST, masterPlaylist)
            putExtra(PlaybackActivity.EXTRA_START_INDEX, if (startIndex != -1) startIndex else 0)
        }
        startActivity(intent)
    }

    private fun navigateToAlbumDetail(album: Album) {
        val intent = Intent(requireActivity(), AlbumDetailActivity::class.java).apply {
            putExtra(AlbumDetailActivity.ALBUM_ID, album.id)
            putExtra(AlbumDetailActivity.ALBUM_NAME, album.name)
        }
        startActivity(intent)
    }

}