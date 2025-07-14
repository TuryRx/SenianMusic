package com.example.senianmusic.ui.artist

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.ui.album.AlbumDetailActivity
import com.example.senianmusic.ui.grid.GridActivity
import com.example.senianmusic.ui.playback.PlaybackActivity
import com.example.senianmusic.ui.presenter.ActionPresenter // <-- IMPORT CORREGIDO
import com.example.senianmusic.ui.presenter.UniversalCardPresenter
import com.example.senianmusic.ui.presenter.ViewMoreAction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistDetailFragment : BrowseSupportFragment() {
    private val TAG = "ArtistDetailFragment"

    private val viewModel: ArtistDetailViewModel by activityViewModels()
    private lateinit var rowsAdapter: ArrayObjectAdapter

    // Creamos los presenters una sola vez para reutilizarlos
    private val cardPresenter = UniversalCardPresenter()
    private val actionPresenter = ActionPresenter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ArtistDetailFragment (BrowseSupportFragment) creado.")

        title = ""
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        showTitle(false)
        view.setBackgroundResource(android.R.color.transparent)

        setupAdapter()
        setupEventListeners()
        observeViewModel()
    }

    private fun setupAdapter() {
        val listRowPresenter = ListRowPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM)
        rowsAdapter = ArrayObjectAdapter(listRowPresenter)
        adapter = rowsAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (!state.isLoading && state.artistName != null) {
                    updateRows(state)
                }
            }
        }
    }

// Dentro de ArtistDetailFragment.kt

    private fun updateRows(state: ArtistDetailUiState) {
        rowsAdapter.clear()
        Log.d(TAG, "Actualizando filas con ${state.topSongs.size} canciones y ${state.albums.size} álbumes.")

        // Fila de Singles
        if (state.topSongs.isNotEmpty()) {
            val songsPresenterSelector = ClassPresenterSelector().apply {
                addClassPresenter(Song::class.java, cardPresenter)
                addClassPresenter(ViewMoreAction::class.java, actionPresenter)
            }
            val songsAdapter = ArrayObjectAdapter(songsPresenterSelector)

            // ¡LÓGICA DE ALEATORIEDAD AQUÍ!
            val songsToDisplay = state.topSongs.shuffled().take(10)
            songsAdapter.setItems(songsToDisplay, null)

            // La condición para "Ver más" ahora es más simple
            if (state.topSongs.size > 10) {
                songsAdapter.add(ViewMoreAction)
            }
            rowsAdapter.add(ListRow(HeaderItem(1L, "Singles"), songsAdapter))
        }

        // Fila de Álbumes
        if (state.albums.isNotEmpty()) {
            val albumsPresenterSelector = ClassPresenterSelector().apply {
                addClassPresenter(Album::class.java, cardPresenter)
                addClassPresenter(ViewMoreAction::class.java, actionPresenter)
            }
            val albumsAdapter = ArrayObjectAdapter(albumsPresenterSelector)

            // ¡LÓGICA DE ALEATORIEDAD AQUÍ!
            val albumsToDisplay = state.albums.shuffled().take(5)
            albumsAdapter.setItems(albumsToDisplay, null)

            if (state.albums.size > 5) {
                albumsAdapter.add(ViewMoreAction)
            }
            rowsAdapter.add(ListRow(HeaderItem(2L, "Álbumes"), albumsAdapter))
        }

        if (rowsAdapter.size() > 0) {
            setSelectedPosition(0, true)
        }
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, row ->
            when (item) {
                is Song -> {
                    val artistPlaylist = ArrayList<Parcelable>(viewModel.uiState.value.topSongs)
                    val startIndex = artistPlaylist.indexOf(item)
                    val intent = Intent(requireActivity(), PlaybackActivity::class.java).apply {
                        action = PlaybackActivity.ACTION_START_PLAYBACK
                        putParcelableArrayListExtra(PlaybackActivity.EXTRA_MASTER_PLAYLIST, artistPlaylist)
                        putExtra(PlaybackActivity.EXTRA_START_INDEX, if (startIndex != -1) startIndex else 0)
                    }
                    startActivity(intent)
                }
                is Album -> {
                    val intent = Intent(requireActivity(), AlbumDetailActivity::class.java).apply {
                        putExtra(AlbumDetailActivity.ALBUM_ID, item.id)
                        putExtra(AlbumDetailActivity.ALBUM_NAME, item.name)
                    }
                    startActivity(intent)
                }
                is ViewMoreAction -> {
                    val listRow = row as? ListRow
                    val headerId = listRow?.headerItem?.id ?: -1

                    if (headerId == 1L) { // "Singles"
                        val uniqueSongs = viewModel.uiState.value.topSongs.distinctBy { it.title.lowercase() }
                        val intent = Intent(requireActivity(), GridActivity::class.java).apply {
                            putExtra(GridActivity.EXTRA_TITLE, "Todas las Canciones")
                            putParcelableArrayListExtra(GridActivity.EXTRA_ITEMS, ArrayList<Parcelable>(uniqueSongs))
                        }
                        startActivity(intent)
                    } else if (headerId == 2L) { // "Álbumes"
                        val uniqueAlbums = viewModel.uiState.value.albums.distinctBy { it.id }
                        val intent = Intent(requireActivity(), GridActivity::class.java).apply {
                            putExtra(GridActivity.EXTRA_TITLE, "Todos los Álbumes")
                            putParcelableArrayListExtra(GridActivity.EXTRA_ITEMS, ArrayList<Parcelable>(uniqueAlbums))
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}