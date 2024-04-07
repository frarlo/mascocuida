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
import java.text.DecimalFormat

class CarersAdapter(private val carerList: MutableList<Carer>): RecyclerView.Adapter<CarersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private var carerName: TextView = view.findViewById(R.id.recycler_carer_name)
        private var carerLocation: TextView = view.findViewById(R.id.recycler_carer_location)
        private var carerRating: TextView = view.findViewById(R.id.recycler_carer_rating)
        private val buttonProfile: Button = view.findViewById(R.id.recycler_carer_profile)
        private val context = view.context

        fun bindCarer(carer: Carer){

            carerName.text = carer.getName()
            carerLocation.text = carer.getLocation()
            val rating = carer.getRating().toString()
            if(rating != "null"){
                val ratingDouble = rating.toDouble()
                // https://stackoverflow.com/a/60565028
                val formattedRatting = "%.2f".format(ratingDouble)
                carerRating.text = formattedRatting
            }else{
                carerRating.text = "-"
            }





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