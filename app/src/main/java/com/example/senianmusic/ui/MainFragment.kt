package com.example.senianmusic.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.R
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.player.PlayerStatus // ¡IMPORTANTE!
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.playback.PlaybackActivity
import com.example.senianmusic.ui.presenter.CardPresenter
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
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
    }

    private fun setupAdapter() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
    }

    // --- ESTA ES LA FUNCIÓN CLAVE CORREGIDA ---
    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, row ->
            // Nos aseguramos de que el clic sea en una canción
            if (item is Song) {
                val listRow = row as ListRow
                val listRowAdapter = listRow.adapter as ArrayObjectAdapter

                val playlist = ArrayList<Song>()
                for (i in 0 until listRowAdapter.size()) {
                    playlist.add(listRowAdapter.get(i) as Song)
                }
                val currentIndex = playlist.indexOf(item)

                // 1. Establecemos el estado global de reproducción.
                // Esto es lo que le dirá a MainActivity que muestre la barra.
                PlayerStatus.setPlaylist(playlist, currentIndex)

                // 2. Lanzamos la actividad de reproducción.
                // Ya no necesita pasarle extras porque leerá todo desde PlayerStatus.
                startActivity(Intent(requireActivity(), PlaybackActivity::class.java))
            }
        }
    }

    private fun observeViewModelAndLoadData() {
        lifecycleScope.launch {
            viewModel.songs.collectLatest { songList ->
                updateSongsRow(songList)
            }
        }
        viewModel.loadInitialData()
    }

    private fun updateSongsRow(songs: List<Song>) {
        val songRowId = 1L
        val headerName = "Canciones Populares"

        // Buscamos si la fila ya existe
        var songRow: ListRow? = null
        for (i in 0 until rowsAdapter.size()) {
            val row = rowsAdapter.get(i) as ListRow
            if (row.headerItem.id == songRowId) {
                songRow = row
                break
            }
        }

        if (songs.isEmpty()) {
            if (songRow != null) {
                rowsAdapter.remove(songRow)
            }
            return
        }

        if (songRow == null) {
            val cardPresenter = CardPresenter()
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            listRowAdapter.setItems(songs, null)
            val header = HeaderItem(songRowId, headerName)
            songRow = ListRow(header, listRowAdapter)
            rowsAdapter.add(songRow)
        } else {
            val listRowAdapter = songRow.adapter as ArrayObjectAdapter
            listRowAdapter.setItems(songs, null)
        }
    }
}