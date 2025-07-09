package com.example.senianmusic.player

import android.content.Context
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import com.example.senianmusic.data.remote.model.Album
import com.example.senianmusic.data.remote.model.Song
import com.example.senianmusic.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PlaybackController {

    private lateinit var musicRepository: MusicRepository
    private lateinit var applicationContext: Context
    private val controllerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initialize(repo: MusicRepository, context: Context) {
        musicRepository = repo
        applicationContext = context.applicationContext
    }

    /**
     * Inicia una nueva lista de reproducción desde el principio.
     * Es llamado por PlaybackActivity cuando recibe un nuevo Intent.
     */
    fun startPlayback(masterList: List<Parcelable>, masterIndex: Int) {
        playItemFromMasterList(masterList, masterIndex, playFromStart = true)
    }

    /**
     * Avanza al siguiente item/canción.
     * Puede ser llamado desde cualquier parte de la app (MainActivity, PlaybackActivity).
     */
    fun goToNext() {
        if (!::musicRepository.isInitialized) return

        if (PlayerStatus.hasNextSongInCurrentItem()) {
            // Hay más canciones en el álbum/item actual
            PlayerStatus.playNextSongInCurrentItem()
            PlayerStatus.currentSong?.let { startSongPlayback(it) }
        } else if (PlayerStatus.hasNextItemInMasterPlaylist()) {
            // Se acabó el item actual, pasamos al siguiente en la lista maestra
            playItemFromMasterList(PlayerStatus.masterPlaylist, PlayerStatus.masterPlaylistIndex + 1, playFromStart = true)
        } else {
            // Fin de toda la lista de reproducción
            controllerScope.launch { showToast("Fin de la lista de reproducción") }
            MusicPlayer.pause()
        }
    }

    /**
     * Retrocede al item/canción anterior.
     * Puede ser llamado desde cualquier parte de la app.
     */
    fun goToPrevious() {
        if (!::musicRepository.isInitialized) return

        if (PlayerStatus.hasPreviousSongInCurrentItem()) {
            // Hay canciones anteriores en el álbum/item actual
            PlayerStatus.playPreviousSongInCurrentItem()
            PlayerStatus.currentSong?.let { startSongPlayback(it) }
        } else if (PlayerStatus.hasPreviousItemInMasterPlaylist()) {
            // Se acabó el item actual, vamos al anterior en la lista maestra
            playItemFromMasterList(PlayerStatus.masterPlaylist, PlayerStatus.masterPlaylistIndex - 1, playFromStart = false)
        }
    }


    private fun playItemFromMasterList(masterList: List<Parcelable>, masterIndex: Int, playFromStart: Boolean) {
        val item = masterList.getOrNull(masterIndex) ?: return

        controllerScope.launch {
            val songsForItem: List<Song>
            val songStartIndex: Int

            when (item) {
                is Song -> {
                    songsForItem = listOf(item)
                    songStartIndex = 0
                }
                is Album -> {
                    songsForItem = musicRepository.fetchAlbumDetails(item.id)
                    songStartIndex = if (playFromStart) 0 else songsForItem.size - 1
                }
                else -> return@launch
            }

            if (songsForItem.isEmpty()) {
                Log.e("PlaybackController", "Item en el índice $masterIndex no tiene canciones. Saltando...")
                showToast("Item vacío, saltando...")
                // Intentar pasar al siguiente/anterior recursivamente
                if (playFromStart) {
                    if (masterIndex + 1 < masterList.size) playItemFromMasterList(masterList, masterIndex + 1, true)
                } else {
                    if (masterIndex - 1 >= 0) playItemFromMasterList(masterList, masterIndex - 1, false)
                }
                return@launch
            }

            // Actualizamos el estado global
            PlayerStatus.setCurrentPlayback(masterList, masterIndex, songsForItem, songStartIndex)
            // Reproducimos la canción
            PlayerStatus.currentSong?.let { startSongPlayback(it) }
        }
    }

    private fun startSongPlayback(song: Song) {
        // Esta corrutina se inicia en el hilo de fondo (IO) para hacer la llamada de red
        controllerScope.launch {
            val streamUrl = musicRepository.getStreamUrlForSong(song)
            if (streamUrl != null) {
                // --- CAMBIO AQUÍ ---
                // Antes de llamar a MusicPlayer.play, cambiamos al hilo principal
                withContext(Dispatchers.Main) {
                    MusicPlayer.play(applicationContext, streamUrl)
                }
                // --- FIN DEL CAMBIO ---
            } else {
                showToast("No se pudo obtener la URL de la canción.")
            }
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}