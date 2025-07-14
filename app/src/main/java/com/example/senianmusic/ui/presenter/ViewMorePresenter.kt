// en com/example/senianmusic/ui/presenter/
package com.example.senianmusic.ui.presenter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.example.senianmusic.R

// Un objeto simple para identificar este tipo de tarjeta
object ViewMoreAction

class ViewMorePresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.action_view_more_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        // No hay datos que vincular, el texto es fijo en el layout
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}