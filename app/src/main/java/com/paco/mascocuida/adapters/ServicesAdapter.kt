package com.paco.mascocuida.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Service
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServicesAdapter(private val servicesMap: HashMap<String, Service>, private val userRole: String,
    private val serviceType: String): RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var linearLayoutOwner: LinearLayout = view.findViewById(R.id.linear_owner)
        private var userFullname: TextView = view.findViewById(R.id.text_userfullname)
        private var userPetName: TextView = view.findViewById(R.id.text_userpet)
        private var buttonPetInfo: Button = view.findViewById(R.id.button_petinfo)
        private var serviceInformation: TextView = view.findViewById(R.id.text_serviceinfo)
        private var serviceFullDate: TextView = view.findViewById(R.id.text_date)
        private var buttonDecline: Button = view.findViewById(R.id.button_decline)
        private var buttonAccept: Button = view.findViewById(R.id.button_accept)
        private val context = view.context                                             // Needed if we want to start a new activity.

        @SuppressLint("SetTextI18n") // Reason: Fullname concatenation should not be subjected to Translation - frarlo
        fun bindService(serviceId: String, service: Service?, userRole: String) {

            // Recuperamos toda la información del servicio con una corrutina:
            CoroutineScope(Dispatchers.Main).launch{
                val userObject = FirebaseDatabaseModel.getOwnerFromFirebase(service?.ownerUid)
                userFullname.text = userObject?.getName() + " " + userObject?.getLastname()
                userPetName.text = service?.pet?.getName()
                serviceInformation.text = service?.information
                serviceFullDate.text = "El día " + service?.date + " a las " + service?.time      // Check for I18n alternatives
            }

            // https://stackoverflow.com/questions/45663126/unconditional-layout-inflation-from-view-adapter-kotlin
            val builder = AlertDialog.Builder(context).setCancelable(true)
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.reusable_popup,null)
            var buttonLeft: Button = view.findViewById(R.id.button_pop_left)
            var buttonRight: Button = view.findViewById(R.id.button_pop_right)
            var popHeader: TextView = view.findViewById(R.id.pop_up_header)
            var popSubHeader: TextView = view.findViewById(R.id.pop_up_subheader)

            builder.setView(view)

            val popUp = builder.create()


            // Dependiendo del rol del usuario y del estatus los botones y la visibilidad de algunos elementos cambiarán:
            if(userRole == "Carer"){

                // Visualización normal de "rechazar" y "aceptar" en caso de que esté pending.

                // Visualización con botón de info si está "accepted"

                // Visualización sin botones si está "rejected" or "completed"


            }else if(userRole == "Owner"){

                // Visualización normal en caso de que esté pending (el botón lanzará un
                linearLayoutOwner.visibility = View.GONE
                buttonDecline.visibility = View.GONE
                buttonAccept.text = "Más info"
            }


            buttonPetInfo.setOnClickListener {
                //TODO - Show all the pet's information stored in the request
            }

            buttonDecline.setOnClickListener {
                //TODO - Pop-up "are you sure?" - update the Service with "status:rejected"
            }

            buttonAccept.setOnClickListener {
                //TODO - Pop-up "are you sure?" - update the Service with "status:accepted"
                popHeader.text = "¿Quieres aceptar la petición?"
                popSubHeader.text = "Ya no podrás rechazarla"
                popUp.show()

                buttonRight.setOnClickListener {
                    // UPDATE SERVICE CRUD METHOD
                    FirebaseDatabaseModel.updateServiceStatus(serviceId,"accepted")
                    Toast.makeText(context,"El estatus del Servicio ahora es 'aceptado'",Toast.LENGTH_SHORT).show()
                }
                buttonLeft.setOnClickListener {
                    // DOES NOTHING - GO BACK
                    popUp.dismiss()
                    Toast.makeText(context,"Se ha pulsado 'Atrás'",Toast.LENGTH_SHORT).show()
                }

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

        // Dependiendo del parámetro que le pasemos al adaptador sólo mostrará los servicios en el estatus elegido:
        when(serviceType){
            "pending" -> {
                if (service?.status == "pending"){
                    holder.bindService(serviceId, service, userRole)
                }
            }
            "accepted" -> {
                if (service?.status == "accepted"){
                    holder.bindService(serviceId, service, userRole)
                }
            }
            "completed" -> {
                if (service?.status == "completed"){
                    holder.bindService(serviceId, service, userRole)
                }
            }
            "rejected" -> {
                if (service?.status == "rejected"){
                    holder.bindService(serviceId, service, userRole)
                }
            }
        }

        //holder.bindService(serviceId, service)
        // TODO - Think about how to filter the different services' status between recyclerviews:
        /*if(!service?.status.equals("Rejected")){
        }*/
    }

}
