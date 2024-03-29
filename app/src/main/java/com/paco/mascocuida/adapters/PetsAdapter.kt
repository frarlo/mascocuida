package com.paco.mascocuida.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Pet

// TODO - Adapter para mostrar las mascotas en el RecyclerView
// https://developer.android.com/develop/ui/views/layout/recyclerview

class PetsAdapter(private val petList: MutableList<Pet>): RecyclerView.Adapter<PetsAdapter.ViewHolder>(){


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        var petName: TextView = view.findViewById(R.id.recycler_pet_name)
        var petSpecies: TextView = view.findViewById(R.id.recycler_pet_species)
        val buttonEdit: Button = view.findViewById(R.id.recycler_pet_editbutton)


        fun bindPet(pet: Pet){
            petName.text = pet.getName()
            petSpecies.text = pet.getSpecies()

            buttonEdit.setOnClickListener {
                //TODO implement selection and edition in "ActivityManagePet"
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.pet_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = petList[position]
        holder.bindPet(pet)
    }


}