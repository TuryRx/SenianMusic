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

        // --- LÍNEA CORREGIDA ---
        // Obtenemos la URL base de algún sitio (por ahora, la ponemos directamente para probar)
        val baseUrl = "http://192.168.1.100:4533/" // <-- ¡CAMBIA ESTO A LA URL DE TU NAVIDROME REAL SI QUIERES VER IMÁGENES!
        val imageUrl = song.getCoverArtUrl(baseUrl)

        Glide.with(viewHolder.view.context)
            .load(imageUrl) // Usamos la URL que construimos
            .centerCrop()
            .error(R.drawable.movie) // Placeholder si la URL es nula o falla
            .into(cardView.mainImageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}