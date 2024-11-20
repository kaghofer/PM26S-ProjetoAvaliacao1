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
    private lateinit var dbHandler: DBHandler // Gerenciador do banco de dados
    private lateinit var btnDeletar: Button
    private lateinit var btnVoltar: Button

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
        btnVoltar = findViewById(R.id.btnVoltar)

        val btnEditar: Button = findViewById(R.id.btnEditar)

        // Recupera o ID do ponto turístico
        val pontoId = intent.getIntExtra("PONTO_TURISTICO_ID", -1)

        if (pontoId != -1) {
            dbHandler = DBHandler(this)
            val ponto = dbHandler.getPontoTuristicoById(pontoId)

            ponto?.let {
                tvNomeData.text = it.name
                tvDescricaoData.text = it.description
                tvEnderecoData.text = it.endereco

                // Exibe a foto, se disponível
                val bitmap = it.photo?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)
                }
                ivFoto2.setImageBitmap(bitmap)

                // Configura o mapa
                mapView.onCreate(savedInstanceState)
                mapView.getMapAsync { googleMap ->
                    val location = LatLng(it.latitude, it.longitude)
                    googleMap.apply {
                        addMarker(MarkerOptions().position(location).title(it.name))
                        moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                        uiSettings.isZoomControlsEnabled = true
                    }
                }

                // Configura o botão de edição
                btnEditar.setOnClickListener {
                    val intent = Intent(this, EditarPontoActivity::class.java)
                    intent.putExtra("PONTO_TURISTICO_ID", pontoId)
                    startActivity(intent)
                }

                btnVoltar.setOnClickListener {
                    val intent = Intent(this, Lista::class.java)
                   startActivity(intent)
                }

                // Configura o botão de exclusão
                btnDeletar.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle("Excluir Ponto Turístico")
                        .setMessage("Tem certeza que deseja excluir este ponto turístico?")
                        .setPositiveButton("Sim") { _, _ ->
                            val rowsDeleted = dbHandler.deletarPontoTuristico(pontoId)
                            if (rowsDeleted > 0) {
                                Toast.makeText(this, "Ponto turístico excluído com sucesso!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, Lista::class.java)
                                startActivity(intent)

                                finish()
                            } else {
                                Toast.makeText(this, "Erro ao excluir ponto turístico!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .create()
                    alertDialog.show()
                }
            }
        } else {
            Toast.makeText(this, "ID do ponto turístico inválido!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}