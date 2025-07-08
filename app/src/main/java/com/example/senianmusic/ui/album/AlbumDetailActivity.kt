package com.example.senianmusic.ui.album

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.senianmusic.R

class AlbumDetailActivity : FragmentActivity() {
    companion object {
        const val ALBUM_ID = "album_id"
        const val ALBUM_NAME = "album_name"
        const val ALBUM_PLAYLIST = "album_playlist"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.album_detail_container, AlbumDetailFragment())
                .commit()
        }
    }
}