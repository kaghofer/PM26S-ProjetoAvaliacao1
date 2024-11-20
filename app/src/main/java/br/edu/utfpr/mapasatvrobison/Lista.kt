package br.edu.utfpr.mapasatvrobison

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class Lista : AppCompatActivity() {

    private lateinit var dbHandler: DBHandler
    private lateinit var btnNovoPonto: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PontoTuristicoAdapter
    private var mapViewBundle: Bundle? = null
    private var isListaVazia = false

    // Inicializando o launcher para o resultado da atividade de edição
    private lateinit var editarPontoLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista)

        // Recuperar estado do MapView se existir
        mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)

        setupViews()
        setupRecyclerView()
        loadInitialData()
        btnNovoPonto = findViewById(R.id.btnNovoPonto)

        // Inicializar o launcher para editar pontos turísticos
        editarPontoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val pontoAtualizado = result.data?.getSerializableExtra("PONTO_TURISTICO") as? PontoTuristico
                pontoAtualizado?.let {
                    updateAdapter(carregarPontosTuristicos())  // Recarregar a lista com os dados atualizados
                }
            }
        }

        escutarBotoes()
    }

    private fun setupViews() {
        dbHandler = DBHandler(this)
        recyclerView = findViewById(R.id.recycleListaPontos)
    }

    private fun escutarBotoes() {
        btnNovoPonto.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PontoTuristicoAdapter(emptyList(), mapViewBundle)
        recyclerView.adapter = adapter
    }

    private fun loadInitialData() {
        val pontos = carregarPontosTuristicos()
        isListaVazia = pontos.isEmpty()
        updateAdapter(pontos)
    }

    private fun updateAdapter(pontos: List<PontoTuristico>) {
        adapter = PontoTuristicoAdapter(pontos, mapViewBundle)
        recyclerView.adapter = adapter
    }

    private fun carregarPontosTuristicos(): List<PontoTuristico> {
        return dbHandler.buscarPontosTuristicos()
    }

    fun buscarPontoPorLocalizacao(latitude: Double, longitude: Double) {
        val pontoTuristico = dbHandler.buscarPontoPorLocalizacao(latitude, longitude)
        if (pontoTuristico != null) {
            updateAdapter(listOf(pontoTuristico))
        } else {
            updateAdapter(carregarPontosTuristicos())
        }
    }

    // Métodos do ciclo de vida para gerenciar os MapViews
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY) ?: Bundle()
        outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
    }

    private fun forEachVisibleHolder(action: (PontoTuristicoAdapter.ViewHolder) -> Unit) {
        for (i in 0 until recyclerView.childCount) {
            val holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i)) as PontoTuristicoAdapter.ViewHolder
            action(holder)
        }
    }

    override fun onStart() {
        super.onStart()
        forEachVisibleHolder { adapter.onStart(it) }
    }

    override fun onResume() {
        super.onResume()
        forEachVisibleHolder { adapter.onResume(it) }
    }

    override fun onPause() {
        forEachVisibleHolder { adapter.onPause(it) }
        super.onPause()
    }

    override fun onStop() {
        forEachVisibleHolder { adapter.onStop(it) }
        super.onStop()
    }

    override fun onDestroy() {
        forEachVisibleHolder { adapter.onDestroy(it) }
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        forEachVisibleHolder { adapter.onLowMemory(it) }
    }
}
