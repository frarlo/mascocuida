package com.paco.mascocuida.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.activities.ManagePetActivity
import com.paco.mascocuida.data.Pet

/*
* Esta clase es un adaptador que nos permite mostrar al usuario, en este caso específico, un listado de Mascotas (pets)
* en una vista de RecyclerView.
*/
class PetsAdapter(private val petMap: HashMap<String, Pet>): RecyclerView.Adapter<PetsAdapter.ViewHolder>(){

    // Clase principal del Adaptador. Representa cada item dentro de la lista (o Mapa):
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        // Inicializamos todos los elementos de la vista:
        private var petName: TextView = view.findViewById(R.id.recycler_pet_name)
        private var petSpecies: TextView = view.findViewById(R.id.recycler_pet_species)
        private val buttonEdit: Button = view.findViewById(R.id.recycler_pet_editbutton)
        private val context = view.context

        // Este método bindea el objeto a la vista:
        fun bindPet(petId: String, pet: Pet?){

            // Mostramos el nombre y la especie del animal:
            petName.text = pet?.getName()
            petSpecies.text = pet?.getSpecies()

            // Listener del botón de edición:
            buttonEdit.setOnClickListener {
                // Extraemos el identificador único de la mascota y lo pasamos como putExtra al intent:
                val petUid = pet?.getPetUid()
                val intent = Intent(context,ManagePetActivity::class.java)
                intent.putExtra("petUid",petUid)
                context.startActivity(intent)
            }
        }
    }

    // Método predeterminado del Adaptador que retorna la vista creada por el adaptador:
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pet_layout, parent, false)
        return ViewHolder(view)
    }

    // Método predeterminado del Adaptador que retorna el tamaño del HashMap de Reviews:
    override fun getItemCount(): Int {
        return petMap.size
    }

    // Método predeterminado que bindeea cada item a la vista llamando al método:
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petId = petMap.keys.toList()[position]
        val pet = petMap[petId]
        holder.bindPet(petId,pet)
    }

}