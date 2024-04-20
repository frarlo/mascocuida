package com.paco.mascocuida.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.activities.ProfileActivity
import com.paco.mascocuida.data.Carer

/*
* Esta clase es un adaptador que nos permite mostrar a los dueños el listado de Cuidadores actuales en Mascocuida en
* un RecyclerView:
*/
class CarersAdapter(private val carerList: MutableList<Carer>): RecyclerView.Adapter<CarersAdapter.ViewHolder>() {

    // Clase principal del Adaptador. Representa cada item dentro de la lista (o MutableList):
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        // Inicializamos todos los elementos de la vista y el contexto por si lo necesitamos:
        private var carerName: TextView = view.findViewById(R.id.recycler_carer_name)
        private var carerLocation: TextView = view.findViewById(R.id.recycler_carer_location)
        private var carerRating: TextView = view.findViewById(R.id.recycler_carer_rating)
        private val buttonProfile: Button = view.findViewById(R.id.recycler_carer_profile)
        private val context = view.context

        // Bindeamos el objeto a la vista:
        fun bindCarer(carer: Carer){

            // Obtenemos nombre, localización y puntuación:
            carerName.text = carer.getName()
            carerLocation.text = carer.getLocation()
            val rating = carer.getRating().toString()

            // Si la puntuación no es nula la pasaremos a doble y la formateamos para que no muestre más de dos decimales:
            if(rating != "null"){
                val ratingDouble = rating.toDouble()
                // https://stackoverflow.com/a/60565028
                val formattedRatting = "%.2f".format(ratingDouble)
                carerRating.text = formattedRatting
            }else{
                // Si es nula no mostraremos un 0, simplemente un "-":
                carerRating.text = "-"
            }

            // Listener que recoge la intención del dueño de ver el perfil del cuidador que ha seleccionado:
            buttonProfile.setOnClickListener {
                // Extraemos el UID del Cuidador y lo pasamos con putExtra al intent:
                val carerUid = carer.getUid()
                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtra("carerUid",carerUid)
                context.startActivity(intent)
            }
        }
    }

    // Método predeterminado del Adaptador que retorna la vista creada por el adaptador:
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.carer_layout, parent, false)
        return ViewHolder(view)
    }

    // Método predeterminado del Adaptador que retorna el tamaño del HashMap de Reviews:
    override fun getItemCount(): Int {
        return carerList.size
    }

    // Método predeterminado que bindeea cada item a la vista llamando al método:
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val carer = carerList[position]
        holder.bindCarer(carer)
    }

}