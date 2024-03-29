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
import com.paco.mascocuida.data.User

class CarersAdapter(private val carerList: MutableList<User>): RecyclerView.Adapter<PetsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private var petName: TextView = view.findViewById(R.id.recycler_pet_name)
        private var petSpecies: TextView = view.findViewById(R.id.recycler_pet_species)
        private val buttonEdit: Button = view.findViewById(R.id.recycler_pet_editbutton)
        private val context = view.context

        fun bindCarer(carer: User){

            // Pet - adapter code:
            //petName.text = pet.getName()
            //petSpecies.text = pet.getSpecies()


            //buttonEdit.setOnClickListener {
            //    //TODO implement selection and edition in "ActivityManagePet"
            //    val petUid = pet.getPetUid()
            //    val intent = Intent(context, ManagePetActivity::class.java)
            //    intent.putExtra("petUid",petUid)
            //    context.startActivity(intent)
            //}

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.pet_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return carerList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val carer = carerList[position]
        holder.bindCarer(carer)
    }


}