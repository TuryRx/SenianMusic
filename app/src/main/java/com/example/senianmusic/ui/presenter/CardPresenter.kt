package com.example.senianmusic.ui.presenter

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.example.senianmusic.data.remote.model.Song

class CardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            // Configura el tamaño de la tarjeta si es necesario
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val song = item as Song
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = song.title
        cardView.contentText = song.artist

        // Aquí usarías una librería como Coil o Glide para cargar la imagen del álbum
        // cardView.mainImageView.load(song.albumArtUrl)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        // Limpia la imagen para evitar que se muestre en otras tarjetas al reciclar
        cardView.mainImage = null
    }
}