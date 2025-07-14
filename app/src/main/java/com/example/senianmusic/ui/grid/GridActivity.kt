package com.example.senianmusic.ui.grid

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.senianmusic.R

class GridActivity : FragmentActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ITEMS = "extra_items"
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_SONG = "type_song"
        const val TYPE_ALBUM = "type_album"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid)

        if (savedInstanceState == null) {
            val fragment = GridFragment()
            fragment.arguments = intent.extras // Pasamos todos los extras al fragmento
            supportFragmentManager.beginTransaction()
                .replace(R.id.grid_frame, fragment)
                .commit()
        }
    }
}