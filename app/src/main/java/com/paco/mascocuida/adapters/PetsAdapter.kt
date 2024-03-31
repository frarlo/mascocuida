package com.paco.mascocuida.adapters

import android.content.Context
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

// TODO - Adapter para mostrar las mascotas en el RecyclerView
// https://developer.android.com/develop/ui/views/layout/recyclerview

class PetsAdapter(private val petMap: HashMap<String, Pet>): RecyclerView.Adapter<PetsAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private var petName: TextView = view.findViewById(R.id.recycler_pet_name)
        private var petSpecies: TextView = view.findViewById(R.id.recycler_pet_species)
        private val buttonEdit: Button = view.findViewById(R.id.recycler_pet_editbutton)
        private val context = view.context

        fun bindPet(petId:String, pet: Pet?){
            petName.text = pet?.getName()
            petSpecies.text = pet?.getSpecies()


            buttonEdit.setOnClickListener {

                val petUid = pet?.getPetUid()
                val intent = Intent(context,ManagePetActivity::class.java)
                intent.putExtra("petUid",petUid)
                context.startActivity(intent)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.pet_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return petMap.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petId = petMap.keys.toList()[position]
        val pet = petMap[petId]
        holder.bindPet(petId,pet)
    }


}