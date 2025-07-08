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
import com.example.senianmusic.player.MusicPlayer
import com.example.senianmusic.player.PlayerStatus
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

    private fun setupEventListeners() {
        // La firma de la lambda ahora es correcta
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, row ->
            when (item) {
                is Song -> {
                    val clickedRow = row as? ListRow ?: return@OnItemViewClickedListener
                    val playlist = ArrayList<Song>()
                    val listRowAdapter = clickedRow.adapter
                    for (i in 0 until listRowAdapter.size()) {
                        if (listRowAdapter.get(i) is Song) {
                            playlist.add(listRowAdapter.get(i) as Song)
                        }
                    }
                    if (playlist.isEmpty()) return@OnItemViewClickedListener
                    val currentIndex = playlist.indexOf(item)
                    PlayerStatus.setPlaylist(playlist, if (currentIndex != -1) currentIndex else 0)
                    lifecycleScope.launch {
                        val url = viewModel.getStreamUrlForSong(item)
                        if (url != null) MusicPlayer.play(requireContext(), url)
                    }
                    startActivity(Intent(requireActivity(), PlaybackActivity::class.java))
                }
                is Album -> {
                    if (item.songCount == 1) {
                        Log.d("MainFragment", "Single detectado: ${item.name}. Reproduciendo...")
                        playSingleAlbum(item)
                    } else {
                        Log.d("MainFragment", "Álbum con ${item.songCount} canciones. Navegando...")
                        navigateToAlbumDetail(item, row as? ListRow)
                    }
                }
            }
        }
    }

    private fun playSingleAlbum(album: Album) {
        lifecycleScope.launch {
            val songs = viewModel.fetchAlbumSongs(album.id)
            if (songs.isNotEmpty()) {
                val singleSong = songs[0]
                PlayerStatus.setPlaylist(arrayListOf(singleSong), 0)
                val url = viewModel.getStreamUrlForSong(singleSong)
                if (url != null) MusicPlayer.play(requireContext(), url)
                startActivity(Intent(requireActivity(), PlaybackActivity::class.java))
            } else {
                Log.e("MainFragment", "No se pudo obtener la canción del single: ${album.name}")
            }
        }
    }

    private fun navigateToAlbumDetail(album: Album, row: ListRow?) {
        val clickedRow = row ?: return
        val albumsInRow = ArrayList<Album>()
        val listRowAdapter = clickedRow.adapter
        for (i in 0 until listRowAdapter.size()) {
            if (listRowAdapter.get(i) is Album) {
                albumsInRow.add(listRowAdapter.get(i) as Album)
            }
        }
        val intent = Intent(requireActivity(), AlbumDetailActivity::class.java).apply {
            putExtra(AlbumDetailActivity.ALBUM_ID, album.id)
            putExtra(AlbumDetailActivity.ALBUM_NAME, album.name)
            putParcelableArrayListExtra(AlbumDetailActivity.ALBUM_PLAYLIST, albumsInRow)
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