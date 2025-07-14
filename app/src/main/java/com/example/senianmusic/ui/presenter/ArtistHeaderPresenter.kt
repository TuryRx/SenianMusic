package com.example.senianmusic.ui.presenter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.senianmusic.R

data class ArtistHeaderInfo(
    val name: String,
    val imageUrl: String?
)

class ArtistHeaderPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.artist_header_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val info = item as ArtistHeaderInfo
        val view = viewHolder.view

        // Estos IDs deben coincidir con los del XML que creaste
        val nameView = view.findViewById<TextView>(R.id.artist_name)
        val imageView = view.findViewById<ImageView>(R.id.artist_image)

        nameView.text = info.name
        Glide.with(view.context)
            .load(info.imageUrl)
            .placeholder(R.drawable.movie) // Imagen mientras carga
            .error(R.drawable.movie)      // Imagen si hay un error
            .into(imageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // No es necesario limpiar aquí
    }
}