package com.example.senianmusic.ui.artist

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.R
import com.example.senianmusic.data.local.AppDatabase
import com.example.senianmusic.data.local.SettingsRepository
import com.example.senianmusic.data.remote.RetrofitClient
import com.example.senianmusic.data.repository.MusicRepository
import com.example.senianmusic.ui.presenter.ArtistDetailsPresenter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistDetailActivity : FragmentActivity() {
    companion object {
        const val ARTIST_ID = "artist_id"
        const val ARTIST_NAME = "artist_name"
    }

    private val TAG = "ArtistDetailActivity"

    private val viewModel: ArtistDetailViewModel by viewModels {
        val repository = MusicRepository(
            context = applicationContext,
            songDao = AppDatabase.getDatabase(this).songDao(),
            settingsRepository = SettingsRepository(this),
            apiService = RetrofitClient.getApiService()
        )
        ArtistDetailViewModelFactory(repository)
    }

    private lateinit var detailsPresenter: ArtistDetailsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_detail)

        val artistId = intent.getStringExtra(ARTIST_ID)
        Log.d(TAG, "Actividad creada para el artista ID: $artistId")

        setupDetailsView()
        observeViewModel()

        if (artistId != null) {
            viewModel.loadArtistData(artistId)
        } else {
            Log.e(TAG, "El ID del artista es NULO. La pantalla no puede funcionar.")
        }
    }

    private fun setupDetailsView() {
        detailsPresenter = ArtistDetailsPresenter(this)
        val detailsContainer = findViewById<ViewGroup>(R.id.artist_details_container)
        val detailsView = detailsPresenter.onCreateView(detailsContainer)
        detailsContainer.addView(detailsView)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (!state.isLoading && state.artistName != null) {
                    Log.d(TAG, "Actualizando el panel de detalles con: ${state.artistName}")
                    detailsPresenter.onBind(state)
                }
            }
        }
    }
}