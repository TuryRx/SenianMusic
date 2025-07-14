package com.example.senianmusic.ui.presenter

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.senianmusic.R
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Song

class GridCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            // Le damos un tamaño fijo para que no se deforme
            setMainImageDimensions(250, 140) // Formato 16:9, puedes ajustar estos valores
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val cardView = viewHolder.view as ImageCardView
        when (item) {
            is Song -> {
                cardView.titleText = item.title
                cardView.contentText = item.artist
                Glide.with(viewHolder.view.context)
                    .load(item.coverArtUrl)
                    .placeholder(R.drawable.movie)
                    .into(cardView.mainImageView)
            }
            is Album -> {
                cardView.titleText = item.name
                cardView.contentText = item.artist
                Glide.with(viewHolder.view.context)
                    .load(item.coverArtUrl)
                    .placeholder(R.drawable.movie)
                    .into(cardView.mainImageView)
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}