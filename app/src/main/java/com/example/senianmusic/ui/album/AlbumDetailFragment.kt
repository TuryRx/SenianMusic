package com.example.senianmusic.ui.album

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
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
import com.example.senianmusic.ui.presenter.UniversalCardPresenter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.senianmusic.data.remote.model.Album // <-- ¡LA SOLUCIÓN ESTÁ AQUÍ!


class AlbumDetailFragment : BrowseSupportFragment() {

    private val viewModel: AlbumDetailViewModel by viewModels {
        val repository = MusicRepository(
            requireActivity().applicationContext,
            AppDatabase.getDatabase(requireContext()).songDao(),
            SettingsRepository(requireContext()),
            RetrofitClient.getApiService()
        )
        AlbumDetailViewModelFactory(repository)
    }

    private lateinit var rowsAdapter: ArrayObjectAdapter
    private val cardPresenter = UniversalCardPresenter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumId = activity?.intent?.getStringExtra(AlbumDetailActivity.ALBUM_ID)
        title = activity?.intent?.getStringExtra(AlbumDetailActivity.ALBUM_NAME) ?: "Álbum"

        headersState = HEADERS_DISABLED // No queremos panel izquierdo aquí

        setupAdapter()
        setupEventListeners()
        observeViewModel()

        if (albumId != null) {
            viewModel.loadAlbumSongs(albumId)
        }
    }

    private fun setupAdapter() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.songs.collectLatest { songs ->
                displaySongs(songs)
            }
        }
    }

    private fun displaySongs(songs: List<Song>) {
        rowsAdapter.clear()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
        listRowAdapter.setItems(songs, null)
        val header = HeaderItem("Canciones del Álbum")
        rowsAdapter.add(ListRow(header, listRowAdapter))
        if(rowsAdapter.size() > 0) setSelectedPosition(0, true)
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Song) {
                // Obtenemos la lista de álbumes que pasamos desde MainFragment
                val albumPlaylist = activity?.intent?.getParcelableArrayListExtra<Album>(AlbumDetailActivity.ALBUM_PLAYLIST) ?: emptyList<Album>()
                val currentAlbumId = activity?.intent?.getStringExtra(AlbumDetailActivity.ALBUM_ID)
                val currentAlbumIndex = albumPlaylist.indexOfFirst { it.id == currentAlbumId }

                // Obtenemos la lista de canciones que ya cargó el ViewModel
                val songsOfThisAlbum = viewModel.songs.value
                val currentSongIndex = songsOfThisAlbum.indexOf(item)

                // Usamos la nueva función para establecer el estado global
                PlayerStatus.setAlbumPlaylist(albumPlaylist, currentAlbumIndex, songsOfThisAlbum, currentSongIndex)

                lifecycleScope.launch {
                    val url = viewModel.getStreamUrlForSong(item)
                    if (url != null) MusicPlayer.play(requireContext(), url)
                }
                startActivity(Intent(requireActivity(), PlaybackActivity::class.java))
            }
        }
    }
}