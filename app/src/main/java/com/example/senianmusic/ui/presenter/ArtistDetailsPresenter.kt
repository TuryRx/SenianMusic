package com.example.senianmusic.ui.presenter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.Presenter
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.senianmusic.R
import com.example.senianmusic.ui.artist.ArtistDetailUiState

// Este presenter NO es para una fila, es para un panel de detalles estático.
class ArtistDetailsPresenter(private val context: Context) {

    private lateinit var view: View
    private lateinit var artistImage: ImageView
    private lateinit var artistName: TextView

    fun onCreateView(container: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        // Inflamos un nuevo layout específico para los detalles
        view = inflater.inflate(R.layout.artist_details_header, container, false)
        artistImage = view.findViewById(R.id.artist_detail_image)
        artistName = view.findViewById(R.id.artist_detail_name)
        return view
    }

    fun onBind(state: ArtistDetailUiState) {
        artistName.text = state.artistName
        Glide.with(context)
            .load(state.artistImageUrl)
            .error(R.drawable.movie)
            .into(artistImage)
    }
}