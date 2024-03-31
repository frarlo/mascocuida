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
import com.paco.mascocuida.activities.ProfileActivity
import com.paco.mascocuida.data.Carer
import com.paco.mascocuida.data.Pet
import com.paco.mascocuida.data.User

class CarersAdapter(private val carerList: MutableList<Carer>): RecyclerView.Adapter<CarersAdapter.ViewHolder>() {
// TODO - Change user - carer etc
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private var carerName: TextView = view.findViewById(R.id.recycler_carer_name)
        private var carerRating: TextView = view.findViewById(R.id.recycler_carer_rating)
        private val buttonProfile: Button = view.findViewById(R.id.recycler_carer_profile)
        private val context = view.context

        fun bindCarer(carer: Carer){

            carerName.text = carer.getName()
            var rating = carer.getRating().toString()
            if(rating == "null"){
                rating = "0"
            }
            carerRating.text = rating



            buttonProfile.setOnClickListener {
                val carerUid = carer.getUid()
                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtra("carerUid",carerUid)
                context.startActivity(intent)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.carer_layout, parent, false)

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