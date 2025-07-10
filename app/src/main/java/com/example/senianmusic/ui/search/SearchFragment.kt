package com.example.senianmusic.ui.search

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast // <-- ¡IMPORTACIÓN AÑADIDA!
import androidx.fragment.app.viewModels
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Artist
import com.example.senianmusic.data.remote.model.SearchResult3
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.ui.album.AlbumDetailActivity
import com.example.senianmusic.ui.playback.PlaybackActivity
import com.example.senianmusic.ui.presenter.ArtistCardPresenter
import com.example.senianmusic.ui.presenter.UniversalCardPresenter
import com.example.senianmusic.ui.search.SearchViewModel
import com.example.senianmusic.ui.search.SearchViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(requireActivity().application)
    }

    private lateinit var rowsAdapter: ArrayObjectAdapter
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        setupEventListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.searchResults.collectLatest { searchResult ->
                displayResults(searchResult)
            }
        }
    }

    override fun onQueryTextChange(newQuery: String?): Boolean {
        currentQuery = newQuery ?: ""
        viewModel.executeSearch(currentQuery)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        currentQuery = query ?: ""
        viewModel.executeSearch(currentQuery)
        return true
    }

    private fun displayResults(result: SearchResult3?) {
        rowsAdapter.clear()

        if (result == null) return

        // 1. Fila de Canciones
        result.songList?.let { songs ->
            if (songs.isNotEmpty()) {
                val listRowAdapter = ArrayObjectAdapter(UniversalCardPresenter())
                listRowAdapter.setItems(songs, null)
                val header = HeaderItem(0, "Canciones")
                rowsAdapter.add(ListRow(header, listRowAdapter))
            }
        }

        // 2. Fila de Álbumes (eliminando duplicados)
        result.albumList?.let { albums ->
            val uniqueAlbums = albums.distinctBy { it.id }
            if (uniqueAlbums.isNotEmpty()) {
                val listRowAdapter = ArrayObjectAdapter(UniversalCardPresenter())
                listRowAdapter.setItems(uniqueAlbums, null)
                val header = HeaderItem(1, "Álbumes")
                rowsAdapter.add(ListRow(header, listRowAdapter))
            }
        }

        // 3. Fila de Artistas
        result.artistList?.let { artists ->
            if (artists.isNotEmpty()) {
                val listRowAdapter = ArrayObjectAdapter(ArtistCardPresenter())
                listRowAdapter.setItems(artists, null)
                val header = HeaderItem(2, "Artistas")
                rowsAdapter.add(ListRow(header, listRowAdapter))
            }
        }
    }

    override fun getResultsAdapter(): ObjectAdapter {
        return rowsAdapter
    }

    private fun setupEventListeners() {
        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Song -> {
                    val playlist = ArrayList<Parcelable>().apply { add(item) }
                    val intent = Intent(requireActivity(), PlaybackActivity::class.java).apply {
                        action = PlaybackActivity.ACTION_START_PLAYBACK
                        putParcelableArrayListExtra(PlaybackActivity.EXTRA_MASTER_PLAYLIST, playlist)
                        putExtra(PlaybackActivity.EXTRA_START_INDEX, 0)
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
                is Artist -> {
                    Toast.makeText(context, "Navegando al artista: ${item.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}