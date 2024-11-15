package br.edu.utfpr.mapasatvrobison

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var btSalvar: Button
    private lateinit var btListar: Button
    private lateinit var ivFoto: ImageView
    private lateinit var btCapturarFoto: Button
    private lateinit var btSelecionarLocalizacao: Button

    private lateinit var locationManager: LocationManager
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var fotoEmBytes: ByteArray? = null

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_LOCATION_PERMISSION = 100 //conferir se nao é 1
    private val REQUEST_MAP = 2




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        solicitarPermissaoLocalizacao()
        escutarBotoes()
    }

    private fun initViews() {
        etNome = findViewById(R.id.etNome)
        etDescricao = findViewById(R.id.etDescricao)
        btSalvar = findViewById(R.id.btSalvar)
        btListar = findViewById(R.id.btListar)
        ivFoto = findViewById(R.id.ivFoto)
        btCapturarFoto = findViewById(R.id.btCapturarFoto)
        btSelecionarLocalizacao = findViewById(R.id.btSelecionarLocalizacao)
    }

    private fun solicitarPermissaoLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        } else {
            iniciarCapturaLocalizacao()
        }
    }


    private fun iniciarCapturaLocalizacao() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10f, this)
        }
    }

    private fun escutarBotoes() {
        btSalvar.setOnClickListener { salvarPontoTuristico() }
        btListar.setOnClickListener { startActivity(Intent(this, Lista::class.java)) }

        btCapturarFoto.setOnClickListener {
            capturarFoto()
        }

        btSelecionarLocalizacao.setOnClickListener {

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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                ivFoto.setImageBitmap(imageBitmap)
                fotoEmBytes = bitmapParaByteArray(imageBitmap)
            } else {
                mostrarMensagem("Erro ao capturar a imagem.")
            }
        } else if (requestCode == REQUEST_MAP && resultCode == Activity.RESULT_OK) {
            val receivedLatitude = data?.getDoubleExtra("LATITUDE", 0.0)
            val receivedLongitude = data?.getDoubleExtra("LONGITUDE", 0.0)

            if (receivedLatitude != null && receivedLongitude != null) {
                latitude = receivedLatitude
                longitude = receivedLongitude
                mostrarMensagem("Localização selecionada: $latitude, $longitude")
            } else {
                mostrarMensagem("Erro ao receber localização do mapa.")
            }
        }
    }

    private fun bitmapParaByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun salvarPontoTuristico() {
        val nome = etNome.text.toString()
        val descricao = etDescricao.text.toString()

        if (nome.isEmpty() || descricao.isEmpty()) {
            mostrarMensagem("Preencha todos os campos e capture uma foto!")
            return
        }

        val pontoTuristico = PontoTuristico(
            name = nome,
            description = descricao,
            latitude = latitude,
            longitude = longitude,
            photo = fotoEmBytes
        )

        val dbHandler = DBHandler(this)
        val success = dbHandler.adicionarPontoTuristico(pontoTuristico)

        mostrarMensagem(if (success > -1) "Ponto turístico salvo!" else "Erro ao salvar o ponto turístico!")
    }

    private fun mostrarMensagem(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarCapturaLocalizacao()
            } else {
                mostrarMensagem("Permissão de localização negada!")
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
