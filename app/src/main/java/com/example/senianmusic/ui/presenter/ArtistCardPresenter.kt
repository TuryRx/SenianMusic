package com.example.senianmusic.ui.presenter

class ArtistCardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val artist = item as Artist
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = artist.name
        cardView.contentText = "Artista" // Texto secundario
        // Usa Coil para cargar la imagen del artista
        // cardView.mainImageView.load(artist.imageUrl)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // Limpia la imagen
    }
}