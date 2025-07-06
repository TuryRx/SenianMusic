package com.example.senianmusic.ui

import androidx.leanback.app.BrowseSupportFragment
// ... otras importaciones

class MainFragment : BrowseSupportFragment() {
    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "SenianMusic"
        // Configura headers, search, etc.

        // Un ObjectAdapter que contendrá filas (ListRow)
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // Creamos un presentador para las tarjetas (ej. artistas)
        val cardPresenter = ArtistCardPresenter()

        // Observamos los datos del ViewModel
        viewModel.artistRows.observe(viewLifecycleOwner) { artistList ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            listRowAdapter.addAll(0, artistList)

            val header = HeaderItem(0, "Artistas")
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }

        adapter = rowsAdapter
    }
}