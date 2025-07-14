package com.example.senianmusic.ui.grid // O el mismo paquete

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.VerticalGridPresenter
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.ui.album.AlbumDetailActivity
import com.example.senianmusic.ui.playback.PlaybackActivity
import com.example.senianmusic.ui.presenter.UniversalCardPresenter
import com.example.senianmusic.ui.presenter.GridCardPresenter


class GridFragment : VerticalGridSupportFragment() {

    private lateinit var itemsAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = arguments?.getString(GridActivity.EXTRA_TITLE) ?: "Elementos"

        setupGrid()
        loadData()
        setupEventListeners()
    }

    private fun setupGrid() {
        // Creamos un presentador de cuadrícula con 5 columnas
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 5
        setGridPresenter(gridPresenter)

        itemsAdapter = ArrayObjectAdapter(GridCardPresenter())
        adapter = itemsAdapter
    }

    private fun loadData() {
        val items = arguments?.getParcelableArrayList<Parcelable>(GridActivity.EXTRA_ITEMS)
        if (items != null) {
            itemsAdapter.setItems(items, null)
        }
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Song -> {
                    // Al hacer clic en una canción, reproducimos solo esa canción
                    val playlist = ArrayList<Parcelable>().apply { add(item) }
                    val intent = Intent(requireActivity(), PlaybackActivity::class.java).apply {
                        action = PlaybackActivity.ACTION_START_PLAYBACK
                        putParcelableArrayListExtra(PlaybackActivity.EXTRA_MASTER_PLAYLIST, playlist)
                        putExtra(PlaybackActivity.EXTRA_START_INDEX, 0)
                    }
                    startActivity(intent)
                }
                is Album -> {
                    // Al hacer clic en un álbum, vamos a su detalle
                    val intent = Intent(requireActivity(), AlbumDetailActivity::class.java).apply {
                        putExtra(AlbumDetailActivity.ALBUM_ID, item.id)
                        putExtra(AlbumDetailActivity.ALBUM_NAME, item.name)
                    }
                    startActivity(intent)
                }
            }
        }
    }
}