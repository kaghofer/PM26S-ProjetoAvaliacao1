package br.edu.utfpr.mapasatvrobison

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import br.edu.utfpr.mapasatvrobison.R
import java.io.ByteArrayOutputStream
class EditarPontoActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etEndereco: EditText
    private lateinit var ivFoto: ImageView
    private lateinit var mapView: MapView
    private lateinit var dbHandler: DBHandler
    private lateinit var btnSalvar: Button
    private lateinit var btnCapturarFoto: Button
    private lateinit var btnSelecionarLocalizacao: Button
    private var pontoId: Int = -1
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var fotoEmBytes: ByteArray? = null

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_MAP = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar)

        // Inicializa os componentes da UI
        etNome = findViewById(R.id.etNome)
        etDescricao = findViewById(R.id.etDescricao)
        etEndereco = findViewById(R.id.etEndereco)
        ivFoto = findViewById(R.id.ivFoto)
        mapView = findViewById(R.id.mapView)
        btnSalvar = findViewById(R.id.btSalvar)
        btnCapturarFoto = findViewById(R.id.btCapturarFoto)
        btnSelecionarLocalizacao = findViewById(R.id.btSelecionarLocalizacao)

        // Recupera o ID do ponto turístico
        pontoId = intent.getIntExtra("PONTO_TURISTICO_ID", -1)

        if (pontoId != -1) {
            dbHandler = DBHandler(this)
            val ponto = dbHandler.getPontoTuristicoById(pontoId)

            ponto?.let {
                // Preenche os campos com as informações existentes
                etNome.setText(it.name)
                etDescricao.setText(it.description)
                etEndereco.setText(it.endereco)

                // Exibe a foto, se disponível
                val bitmap = it.photo?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)
                }
                ivFoto.setImageBitmap(bitmap)

                // Define as coordenadas para o mapa
                latitude = it.latitude
                longitude = it.longitude

                // Configura o mapa
                mapView.onCreate(savedInstanceState)
                mapView.getMapAsync { googleMap ->
                    val location = LatLng(latitude, longitude)
                    googleMap.apply {
                        addMarker(MarkerOptions().position(location).title(it.name))
                        moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                        uiSettings.isZoomControlsEnabled = true
                    }
                }
            }
        } else {
            Toast.makeText(this, "ID do ponto turístico inválido!", Toast.LENGTH_SHORT).show()
            finish()
        }


        // Configura o botão de salvar
        btnSalvar.setOnClickListener {
            val nome = etNome.text.toString()
            val descricao = etDescricao.text.toString()
            val endereco = etEndereco.text.toString()

            if (nome.isNotEmpty() && descricao.isNotEmpty() && endereco.isNotEmpty()) {
                // Atualiza as informações no banco de dados
                val pontoAtualizado = PontoTuristico(pontoId, nome, descricao, endereco, latitude, longitude, fotoEmBytes)
                val rowsUpdated = dbHandler.atualizarPontoTuristico(pontoAtualizado)

                if (rowsUpdated > 0) {
                    Toast.makeText(this, "Ponto turístico atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK, intent)
                    finish()

                    val intent = Intent(this, Item::class.java)
                    intent.putExtra("PONTO_TURISTICO_ID", pontoId)
                    startActivity(intent)


                } else {
                    Toast.makeText(this, "Erro ao atualizar ponto turístico!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura o botão de captura de foto
        btnCapturarFoto.setOnClickListener {
            capturarFoto()
        }

        // Configura o botão de seleção de localização
        btnSelecionarLocalizacao.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivityForResult(intent, REQUEST_MAP)
        }
    }

    private fun capturarFoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        ivFoto.setImageBitmap(it)
                        fotoEmBytes = bitmapParaByteArray(it)
                    }
                }
            }
            REQUEST_MAP -> {
                if (resultCode == Activity.RESULT_OK) {
                    latitude = data?.getDoubleExtra("LATITUDE", 0.0) ?: 0.0
                    longitude = data?.getDoubleExtra("LONGITUDE", 0.0) ?: 0.0
                    mostrarMensagem("Localização selecionada: $latitude, $longitude")
                }
            }
        }
    }

    private fun bitmapParaByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun mostrarMensagem(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}