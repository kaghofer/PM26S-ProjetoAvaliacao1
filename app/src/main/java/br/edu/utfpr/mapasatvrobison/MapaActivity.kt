package br.edu.utfpr.mapasatvrobison

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null //variavel para armazenar a ultima localizacao conhecida

    private val defaultLocation = LatLng(-26.2286100, -52.6705600) //define uma loc padrao, caso nao encontre outra... loc de pato branco

    private val DEFAULT_ZOOM = 20 //define um zoom padrao para o mapa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)


        // Obtém o fragmento de mapa do layout e configura o callback
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Constroi o fusedLocation do cliente para pegar a loc atual
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onMapReady(map: GoogleMap) {
        getDeviceLocation()
        googleMap = map
        // Configura o clique no mapa para capturar a localização
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear() // Limpa o mapa de marcadores anteriores

            // Adiciona um marcador no local clicado
            googleMap.addMarker(
                MarkerOptions().position(latLng).title("Local selecionado")
            )
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            // Retorna as coordenadas para a MainActivity
            val resultIntent = Intent().apply {
                putExtra("LATITUDE", latLng.latitude)
                putExtra("LONGITUDE", latLng.longitude)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }

    private fun getDeviceLocation() {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Define a posição da câmera do mapa para a localização atual do dispositivo.
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        val latLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)

                        // Move a câmera para a localização atual
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))

                        // Adiciona um marcador na localização atual
                        googleMap?.addMarker(
                            MarkerOptions().position(latLng).title("Local selecionado")
                        )
                    }
                } else {
                    Log.d(TAG, "Localizacao atual e nula, usando a default")
                    Log.e(TAG, "Exception: %s", task.exception)
                    googleMap?.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                    googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

}
