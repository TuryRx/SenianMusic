package com.example.senianmusic.ui.presenter

import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter

// Heredamos del constructor vacío
class CustomListRowPresenter : ListRowPresenter() {

    override fun initializeRowViewHolder(holder: RowPresenter.ViewHolder) {
        super.initializeRowViewHolder(holder)
        holder.view.setBackgroundResource(android.R.color.transparent)
    }
}