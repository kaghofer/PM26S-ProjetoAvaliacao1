package br.edu.utfpr.mapasatvrobison

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etEndereco: EditText
    private lateinit var btSalvar: Button
    private lateinit var btListar: Button
    private lateinit var btConfiguracao: Button
    private lateinit var ivFoto: ImageView
    private lateinit var btCapturarFoto: Button
    private lateinit var btSelecionarLocalizacao: Button

    private lateinit var locationManager: LocationManager
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var fotoEmBytes: ByteArray? = null
    private var enderecoInteiro: String = ""

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_LOCATION_PERMISSION = 100
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
        btConfiguracao = findViewById(R.id.btnConfiguracao)
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
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10f, this)
        }
    }
    private fun escutarBotoes() {
        btSalvar.setOnClickListener { salvarPontoTuristico() }

        btListar.setOnClickListener {
            startActivity(Intent(this, Lista::class.java))
        }

        btCapturarFoto.setOnClickListener {
            capturarFoto()
        }

        btSelecionarLocalizacao.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivityForResult(intent, REQUEST_MAP)
        }

        btConfiguracao.setOnClickListener{
            startActivity(Intent(this, activity_settings::class.java))
        }
    }
    private fun obterEndereco(latitude: Double, longitude: Double, callback: (String) -> Unit) {
        // Rodando em uma thread separada
        Thread {
            var endereco: String? = null
            try {
                val urlString = "https://maps.googleapis.com/maps/api/geocode/xml?latlng=$latitude,$longitude&key=AIzaSyDYI_NTGuMeCeqYrDQrDZvdjMzStWI9Ln4"
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.apply {
                    requestMethod = "GET"
                    connectTimeout = 15000 // Timeout de conexão
                    readTimeout = 15000 // Timeout de leitura
                    doInput = true
                }

                // Recebe o InputStream da resposta
                val inputStream = urlConnection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                // Processamento do XML para extrair o endereço
                val localRegex = "<formatted_address>(.*?)</formatted_address>".toRegex()
                endereco = localRegex.find(response.toString())?.groups?.get(1)?.value

                // Se o endereço for encontrado, passa para o callback
                callback(endereco ?: "Endereço não encontrado")
            } catch (e: Exception) {
                Log.e("obterEndereco", "Erro ao buscar endereço: ${e.message}")
                // Exibe mensagem de erro na thread principal
                runOnUiThread {
                    mostrarMensagem("Erro ao buscar endereço. Verifique sua conexão.")
                }
                callback("Erro ao buscar endereço")
            }
        }.start()
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

    private fun salvarPontoTuristico() {
        val nome = etNome.text.toString()
        val descricao = etDescricao.text.toString()

        if (nome.isEmpty() || descricao.isEmpty() ) {
            mostrarMensagem("Preencha todos os campos e capture uma foto!")
            return
        }

        if (latitude == 0.0 || longitude == 0.0) {
            mostrarMensagem("Selecione a localização primeiro!")
            return
        }

        // Obter o endereço com callback
        obterEndereco(latitude, longitude) { endereco ->
            runOnUiThread {
                if (endereco == "Endereço não encontrado" || endereco.startsWith("Erro")) {
                    mostrarMensagem(endereco)
                    return@runOnUiThread
                }

                enderecoInteiro = endereco

                val pontoTuristico = PontoTuristico(
                    name = nome,
                    description = descricao,
                    latitude = latitude,
                    longitude = longitude,
                    photo = fotoEmBytes,
                    endereco = enderecoInteiro
                )

                val dbHandler = DBHandler(this)
                val success = dbHandler.adicionarPontoTuristico(pontoTuristico)

                mostrarMensagem("Ponto turístico salvo!")

                val intent = Intent(this, Lista::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
 

    private fun mostrarMensagem(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 10)
        toast.show()
    }


    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarCapturaLocalizacao()
        }


    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
