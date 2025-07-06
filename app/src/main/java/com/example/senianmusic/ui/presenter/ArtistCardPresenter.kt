package com.example.senianmusic.ui.presenter

// --- IMPORTS NECESARIOS ---
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.senianmusic.R
import com.example.senianmusic.data.remote.model.Artist
// -------------------------

class ArtistCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(313, 313) // Hacemos las tarjetas cuadradas para artistas
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val artist = item as Artist
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = artist.name
        cardView.contentText = "Artista" // Texto secundario

        // Usa Glide para cargar la imagen del artista
        Glide.with(viewHolder.view.context)
            .load(artist.imageUrl)
            .centerCrop()
            .error(R.drawable.movie) // Un placeholder genérico, puedes cambiarlo
            .into(cardView.mainImageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        // Limpia la imagen para liberar memoria
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}