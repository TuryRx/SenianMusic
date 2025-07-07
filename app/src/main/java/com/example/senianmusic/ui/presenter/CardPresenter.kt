package com.example.senianmusic.ui.presenter

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.senianmusic.R
import com.example.senianmusic.data.remote.model.Song

class CardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val song = item as Song
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = song.title
        cardView.contentText = song.artist
        cardView.setMainImageDimensions(313, 176)

        // ¡Esta línea ahora funciona!
        // El repositorio ya se encargó de poner la URL completa en 'song.coverArtUrl'.
        Glide.with(viewHolder.view.context)
            .load(song.coverArtUrl)
            .centerCrop()
            .error(R.drawable.movie)
            .into(cardView.mainImageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}