// Asegúrate de que el paquete sea el correcto
package com.example.senianmusic.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.senianmusic.R
import com.example.senianmusic.data.remote.model.Song // O el modelo que uses, como Artist
import com.example.senianmusic.ui.main.MainViewModel
import com.example.senianmusic.ui.main.MainViewModelFactory
import com.example.senianmusic.ui.presenter.CardPresenter // Asegúrate de que esta clase exista
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainFragment : BrowseSupportFragment() {

    private val viewModel: MainViewModel by viewModels {
        // Asegúrate de que MainViewModelFactory exista y funcione
        MainViewModelFactory(requireContext().applicationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "SenianMusic"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = requireContext().getColor(R.color.fastlane_background) // Asegúrate de tener este color en colors.xml
        searchAffordanceColor = requireContext().getColor(R.color.search_opaque) // Y este también

        setupEventListeners()
        setupRows()
        observeViewModel()
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            // Aquí manejas el clic en cualquier tarjeta
            if (item is Song) { // Cambia 'Song' por el tipo de dato que estés mostrando
                Toast.makeText(requireContext(), "Clicked on: ${item.title}", Toast.LENGTH_SHORT).show()
                // Aquí podrías navegar a otra pantalla o reproducir
            }
        }
    }

    private fun setupRows() {
        // Un ObjectAdapter que contendrá todas las filas (ListRow)
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // --- Ejemplo de cómo añadir una fila ---
        val cardPresenter = CardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)

        val header = HeaderItem(0, "Canciones Populares")
        rowsAdapter.add(ListRow(header, listRowAdapter))
        // -------------------------------------

        adapter = rowsAdapter
    }

    private fun observeViewModel() {
        // Observa los datos del ViewModel
        lifecycleScope.launch {
            viewModel.songs.collectLatest { songList ->
                // Obtenemos el adapter de la primera fila
                val listRow = adapter.get(0) as ListRow
                val listRowAdapter = listRow.adapter as ArrayObjectAdapter

                // Actualizamos los datos de la fila
                listRowAdapter.setItems(songList, null)
            }
        }
    }
}