package br.edu.utfpr.mapasatvrobison

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-26.2286100, -52.6705600)
    private val LOCATION_PERMISSION_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        checkLocationPermissions()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (!hasPermissions()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    private fun hasPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        getDeviceLocation()

        //Pega o valor padrao de zoom do sharedPreferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val mapType = sharedPreferences.getString("map_type", "normal") //se nao tiver valor, seta o normal
        val defaultZoom = sharedPreferences.getInt("default_map_zoom", 15)

        //percorre o maptype e seta o tipo de mapa de acordo com a key vindo do sharedPreferences
        when (mapType) {
            "normal" -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            "satellite" -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            "hybrid" -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            "terrain" -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }

        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Local selecionado"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom.toFloat()))

            val resultIntent = Intent().apply {
                putExtra("LATITUDE", latLng.latitude)
                putExtra("LONGITUDE", latLng.longitude)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun getDeviceLocation() {

        //Pega o valor padrao de zoom do sharedPreferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultZoom = sharedPreferences.getInt("default_map_zoom", 15)
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    lastKnownLocation = task.result
                    val latLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom.toFloat()))
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom.toFloat()))
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}
