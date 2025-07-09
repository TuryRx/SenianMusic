// en ui/presenter/ActionPresenter.kt
package com.example.senianmusic.ui.presenter

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.example.senianmusic.R

class ActionPresenter : Presenter() {

    // Definimos un tamaño fijo para que todos los botones sean iguales
    private companion object {
        private const val BUTTON_WIDTH = 320 // Ancho en píxeles
        private const val BUTTON_HEIGHT = 100 // Alto en píxeles
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val textView = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(BUTTON_WIDTH, BUTTON_HEIGHT)
            isFocusable = true
            isFocusableInTouchMode = true
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(context, R.drawable.action_background)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium) // Un poco más pequeño que Title
            // Para que se vea bien el texto si es largo
            maxLines = 1
        }
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val textView = viewHolder.view as TextView
        textView.text = item as? String ?: ""
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // No es necesario hacer nada aquí
    }
}