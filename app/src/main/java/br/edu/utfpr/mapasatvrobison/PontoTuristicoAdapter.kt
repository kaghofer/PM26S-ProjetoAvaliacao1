package br.edu.utfpr.mapasatvrobison

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PontoTuristicoAdapter(
    private val pontos: List<PontoTuristico>,
    private val mapViewBundle: Bundle?,
) : RecyclerView.Adapter<PontoTuristicoAdapter.ViewHolder>() {

    private val mapStateMap = mutableMapOf<Int, Bundle?>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeTextView: TextView = itemView.findViewById(R.id.tvNomeData)
        val descricaoTextView: TextView = itemView.findViewById(R.id.tvDescricaoData)
        val fotoImageView: ImageView = itemView.findViewById(R.id.ivFoto2)
        val enderecoTextView: TextView = itemView.findViewById(R.id.tvEnderecoData)
        val mapView: MapView = itemView.findViewById(R.id.mapView)
        val btnDetalhes: Button = itemView.findViewById(R.id.btnDetalhes)
        var googleMap: GoogleMap? = null

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_lista, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = pontos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ponto = pontos[position]

        // Configurar informações básicas
        holder.nomeTextView.text = ponto.name
        holder.enderecoTextView.text = ponto.endereco
        holder.descricaoTextView.text = ponto.description

        // Configurar a foto
        val bitmap = ponto.photo?.let {
            BitmapFactory.decodeByteArray(ponto.photo, 0, it.size)
        }
        holder.fotoImageView.setImageBitmap(bitmap)

        // Configurar o MapView
        with(holder.mapView) {
            tag = position // Identificar o MapView para gerenciamento de estado
            onCreate(mapStateMap[position])

            getMapAsync { googleMap ->
                holder.googleMap = googleMap

                // Configurar os controles do mapa
                googleMap.uiSettings.apply {
                    isZoomControlsEnabled = true
                    isScrollGesturesEnabled = true
                    isZoomGesturesEnabled = true
                }

                // Criar o LatLng para o ponto turístico
                val location = LatLng(ponto.latitude, ponto.longitude)

                // Limpar o mapa e adicione o marcador
                googleMap.apply {
                    clear()
                    addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(ponto.name)
                    )
                    moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                }
            }
        }
        holder.btnDetalhes.setOnClickListener {
            val ponto = pontos[position]
            val intent = Intent(holder.itemView.context, Item::class.java)
            intent.putExtra("PONTO_TURISTICO_ID", ponto.id)
            holder.itemView.context.startActivity(intent)
        }

    }

    // Métodos do ciclo de vida do MapView
    fun onResume(holder: ViewHolder) {
        holder.mapView.onResume()
    }

    fun onStart(holder: ViewHolder) {
        holder.mapView.onStart()
    }

    fun onStop(holder: ViewHolder) {
        holder.mapView.onStop()
    }

    fun onPause(holder: ViewHolder) {
        holder.mapView.onPause()
    }

    fun onDestroy(holder: ViewHolder) {
        holder.mapView.onDestroy()
    }

    fun onLowMemory(holder: ViewHolder) {
        holder.mapView.onLowMemory()
    }

    // Salvar estado do mapa quando a view é reciclada
    override fun onViewRecycled(holder: ViewHolder) {
        val position = holder.mapView.tag as Int
        val bundle = Bundle()
        holder.mapView.onSaveInstanceState(bundle)
        mapStateMap[position] = bundle

        holder.googleMap?.clear()
        holder.mapView.onDestroy()
        super.onViewRecycled(holder)
    }
}