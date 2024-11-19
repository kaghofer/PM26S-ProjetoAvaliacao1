package br.edu.utfpr.mapasatvrobison
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Item : AppCompatActivity() {
    private lateinit var tvNome2: TextView
    private lateinit var tvNomeData: TextView
    private lateinit var tvDescricao2: TextView
    private lateinit var tvDescricaoData: TextView
    private lateinit var tvEndereco2: TextView
    private lateinit var tvEnderecoData: TextView
    private lateinit var ivFoto2: ImageView
    private lateinit var mapView: MapView
    private lateinit var dbHandler: DBHandler  // Seu DBHandler, que gerencia o banco de dados
    private lateinit var btnDeletar: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        // Inicialize os componentes da UI
        tvNome2 = findViewById(R.id.tvNome2)
        tvNomeData = findViewById(R.id.tvNomeData)
        tvDescricao2 = findViewById(R.id.tvDescricao2)
        tvDescricaoData = findViewById(R.id.tvDescricaoData)
        tvEndereco2 = findViewById(R.id.tvEndereco2)
        tvEnderecoData = findViewById(R.id.tvEnderecoData)
        ivFoto2 = findViewById(R.id.ivFoto2)
        mapView = findViewById(R.id.mapView)
        btnDeletar = findViewById(R.id.btnDeletar)
        // Recuperar o ID passado pelo Intent
        val pontoId = intent.getIntExtra("PONTO_TURISTICO_ID", -1)

        // Verificar se o ID é válido
        if (pontoId != -1) {
            // Buscar o ponto turístico no banco de dados
            dbHandler = DBHandler(this)  // Supondo que o DBHandler já esteja configurado
            val ponto = dbHandler.getPontoTuristicoById(pontoId)

            // Preencher os dados nos campos da tela
            ponto?.let {
                tvNomeData.text = it.name
                tvDescricaoData.text = it.description
                tvEnderecoData.text = it.endereco

                // Setar a foto
                val bitmap = ponto.photo?.let {
                    BitmapFactory.decodeByteArray(ponto.photo, 0, it.size)
                }
                ivFoto2.setImageBitmap(bitmap)

                // Configure o mapa
                mapView.onCreate(savedInstanceState)  // Inicializa o MapView
                mapView.getMapAsync { googleMap ->
                    val location = LatLng(it.latitude, it.longitude)
                    googleMap.apply {
                        addMarker(MarkerOptions().position(location).title(it.name))
                        moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                        uiSettings.isZoomControlsEnabled = true
                    }
                }
                btnDeletar.setOnClickListener {

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Confirmação")
                    builder.setMessage("Tem certeza de que deseja deletar este ponto turístico?")
                    // Caso o usuário clique em "Sim"
                    builder.setPositiveButton("Sim") { dialog, which ->
                        val deleted = dbHandler.deletarPontoTuristico(pontoId)
                        Toast.makeText(this, "Ponto turístico deletado com sucesso!", Toast.LENGTH_SHORT).show()

                        // Enviar um Intent para a tela de listagem para atualizar a lista
                        val intent = Intent(this, Lista::class.java)
                        startActivity(intent)
                        finish()
                    }

                    // Caso o usuário clique em "Não"
                    builder.setNegativeButton("Não") { dialog, which ->
                        dialog.dismiss()  // Fechar o dialog sem fazer nada
                    }

                    // Mostrar o dialog
                    builder.show()


                }
            }
        }
    }

    // Ciclo de vida do MapView
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}