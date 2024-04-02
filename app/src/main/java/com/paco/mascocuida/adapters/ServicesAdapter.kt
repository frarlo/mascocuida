package com.paco.mascocuida.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Service
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServicesAdapter(private val servicesMap: HashMap<String, Service>): RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var userFullname: TextView = view.findViewById(R.id.text_userfullname)
        private var userPetName: TextView = view.findViewById(R.id.text_userpet)
        private var buttonPetInfo: Button = view.findViewById(R.id.button_petinfo)
        private var serviceInformation: TextView = view.findViewById(R.id.text_serviceinfo)
        private var serviceFullDate: TextView = view.findViewById(R.id.text_date)
        private var buttonDecline: Button = view.findViewById(R.id.button_decline)
        private var buttonAccept: Button = view.findViewById(R.id.button_accept)
        private val context = view.context                                             // Needed if we want to start a new activity.

        @SuppressLint("SetTextI18n") // Reason: Fullname concatenation should not be subjected to Translation - frarlo
        fun bindService(serviceId: String, service: Service?) {

            CoroutineScope(Dispatchers.Main).launch{
                val userObject = FirebaseDatabaseModel.getOwnerFromFirebase(service?.ownerUid)
                userFullname.text = userObject?.getName() + " " + userObject?.getLastname()
                userPetName.text = service?.pet?.getName()
                serviceInformation.text = service?.information
                serviceFullDate.text = "El día " + service?.date + " a las " + service?.time      // Check for I18n alternatives
            }

            buttonPetInfo.setOnClickListener {
                //TODO - Show all the pet's information stored in the request
            }

            buttonDecline.setOnClickListener {
                //TODO - Pop-up "are you sure?" - update the Service with "status:rejected"
            }

            buttonAccept.setOnClickListener {
                //TODO - Pop-up "are you sure?" - update the Service with "status:accepted"
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.service_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return servicesMap.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val serviceId = servicesMap.keys.toList()[position]
        val service = servicesMap[serviceId]
        holder.bindService(serviceId, service)

        // TODO - Think about how to filter the different services' status between recyclerviews:
        /*if(!service?.status.equals("Rejected")){
        }*/
    }
}
