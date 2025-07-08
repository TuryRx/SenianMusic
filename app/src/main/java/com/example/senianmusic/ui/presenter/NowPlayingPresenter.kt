package com.example.senianmusic.ui.presenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.example.senianmusic.R
import com.example.senianmusic.data.remote.model.Song

// Objeto de datos que pasaremos a este Presenter.
data class NowPlayingInfo(
    val song: Song,
    val progress: Long,
    val duration: Long,
    val coverUrl: String?
)

class NowPlayingPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        // Usamos un layout personalizado para la tarjeta "Ahora Suena".
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.now_playing_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val info = item as NowPlayingInfo
        val view = viewHolder.view

        val titleView = view.findViewById<TextView>(R.id.tv_now_playing_title)
        val artistView = view.findViewById<TextView>(R.id.tv_now_playing_artist)
        val coverView = view.findViewById<ImageView>(R.id.iv_now_playing_cover)
        val progressBar = view.findViewById<ProgressBar>(R.id.now_playing_progress)

        titleView.text = info.song.title
        artistView.text = info.song.artist

        if (info.duration > 0) {
            progressBar.max = info.duration.toInt()
            progressBar.progress = info.progress.toInt()
        } else {
            progressBar.max = 1
            progressBar.progress = 0
        }

        Glide.with(view.context)
            .load(info.coverUrl)
            .placeholder(R.drawable.movie) // Imagen por defecto
            .into(coverView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // No es necesario limpiar nada aquí.
    }
}