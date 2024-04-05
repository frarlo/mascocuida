package com.paco.mascocuida.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Owner
import com.paco.mascocuida.data.Review
import com.paco.mascocuida.data.Service
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServicesAdapter(private val servicesMap: Map<String, Service>, private val userRole: String,
    private val serviceType: String): RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var linearLayoutOwner: LinearLayout = view.findViewById(R.id.linear_owner)
        private var userFullname: TextView = view.findViewById(R.id.text_userfullname)
        private var userPetName: TextView = view.findViewById(R.id.text_userpet)
        private var buttonPetInfo: Button = view.findViewById(R.id.button_petinfo)
        private var linearLayoutInformation: LinearLayout = view.findViewById(R.id.linear_information)
        private var serviceInformation: TextView = view.findViewById(R.id.text_serviceinfo)
        private var serviceFullDate: TextView = view.findViewById(R.id.text_date)
        private var buttonDecline: Button = view.findViewById(R.id.button_decline)
        private var buttonAccept: Button = view.findViewById(R.id.button_accept)
        private val context = view.context                                             // Needed if we want to start a new activity.

        @SuppressLint("SetTextI18n") // Reason: Fullname concatenation should not be subjected to Translation - frarlo
        fun bindService(serviceId: String, service: Service?, userRole: String, serviceType: String) {

            var userObject: Owner? = null

            // Recuperamos toda la información del servicio con una corrutina:
            CoroutineScope(Dispatchers.Main).launch{
                userObject = FirebaseDatabaseModel.getOwnerFromFirebase(service?.ownerUid)
                userFullname.text = userObject?.getName() + " " + userObject?.getLastname()
                userPetName.text = service?.pet?.getName()
                serviceInformation.text = service?.information
                serviceFullDate.text = "El día " + service?.date + " a las " + service?.time      // Check for I18n alternatives
            }

            // https://stackoverflow.com/questions/45663126/unconditional-layout-inflation-from-view-adapter-kotlin
            val builder = AlertDialog.Builder(context).setCancelable(true)
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.reusable_popup,null)
            val buttonLeft: Button = view.findViewById(R.id.button_pop_left)
            val buttonRight: Button = view.findViewById(R.id.button_pop_right)
            val popHeader: TextView = view.findViewById(R.id.pop_up_header)
            val popSubHeader: TextView = view.findViewById(R.id.pop_up_subheader)

            builder.setView(view)
            val popUp = builder.create()


            // Dependiendo del rol del usuario y del estado del servicio los botones y la visibilidad de algunos elementos cambiarán:
            if(userRole == "Carer"){
                when(serviceType){
                    "pending" -> {

                    }
                    "accepted" -> {
                        buttonDecline.visibility = View.GONE
                        buttonAccept.text = "Más info"
                    }
                    "completed" -> {
                        buttonDecline.visibility = View.GONE
                        buttonAccept.visibility = View.GONE
                    }
                    "rejected" -> {

                    }
                }

            }else if(userRole == "Owner"){
                linearLayoutOwner.visibility = View.GONE
                when(serviceType){
                    "pending" -> {
                        buttonAccept.visibility = View.GONE
                        buttonDecline.text = "Cancelar solicitud"
                    }
                    "accepted" -> {
                        buttonDecline.visibility = View.GONE
                        buttonAccept.text = "Más info"
                    }
                    "completed" -> {
                        buttonDecline.visibility = View.GONE
                        linearLayoutInformation.visibility = View.GONE
                        buttonAccept.text = "Opina sobre el cuidado"
                    }
                    "rejected" -> {

                    }
                }
                // Visualización normal en caso de que esté pending (el botón lanzará un

            }


            buttonPetInfo.setOnClickListener {
                //TODO - Show all the pet's information stored in the request - Independent from userRole and service status
            }

            buttonDecline.setOnClickListener {
                //TODO - Pop-up "are you sure?" - update the Service with "status:rejected"

                if(userRole == "Carer"){
                    popHeader.text = "¿Quieres rechazar la petición?"
                    popSubHeader.text = "Ya no podrás aceptarla"
                    popUp.show()

                    buttonRight.setOnClickListener {
                        // UPDATE SERVICE CRUD METHOD
                        FirebaseDatabaseModel.updateServiceStatus(serviceId,"rejected")
                        Toast.makeText(context,"El estatus del Servicio ahora es 'rechazado'",Toast.LENGTH_SHORT).show()
                        popUp.dismiss()
                    }
                    buttonLeft.setOnClickListener {
                        // DOES NOTHING - GO BACK
                        popUp.dismiss()
                        Toast.makeText(context,"Se ha pulsado 'Atrás'",Toast.LENGTH_SHORT).show()
                    }
                }

                if(userRole == "Owner"){
                    popHeader.text = "¿Quieres cancelar la petición?"
                    popSubHeader.text = "Tendrás que realizarla de nuevo"
                    popUp.show()

                    buttonRight.setOnClickListener {
                        // yes
                        FirebaseDatabaseModel.removeService(serviceId)
                        popUp.dismiss()
                    }
                    buttonLeft.setOnClickListener {
                        popUp.dismiss()
                    }
                }

            }

            buttonAccept.setOnClickListener {

                if(userRole == "Carer"){
                    if(serviceType == "pending"){
                        popHeader.text = "¿Quieres aceptar la petición?"
                        popSubHeader.text = "Ya no podrás rechazarla"
                        popUp.show()

                        buttonRight.setOnClickListener {
                            // UPDATE SERVICE CRUD METHOD
                            FirebaseDatabaseModel.updateServiceStatus(serviceId,"accepted")
                        }
                        buttonLeft.setOnClickListener {
                            popUp.dismiss()
                        }
                    }else if(serviceType == "accepted"){
                        buttonRight.setOnClickListener{
                            Toast.makeText(context,"Carer quiere información",Toast.LENGTH_SHORT).show()

                        }
                    }

                }

                if(userRole == "Owner"){
                    if(serviceType == "accepted"){
                        Toast.makeText(context,"Owner quiere información",Toast.LENGTH_SHORT).show()

                    }else if(serviceType == "completed"){


                        // 1. Captamos el que sería el author de la review y el ID del cuidador que la recibe:
                        val author = userObject?.getName()
                        val carerId = service?.carerUid




                        // 2. Comprobamos si ya existe una review de este servicio:
                        CoroutineScope(Dispatchers.Main).launch {
                            if(carerId != null){
                                // Checking if a review of THIS service already exists:
                                val existsReview = FirebaseDatabaseModel.checkCarerReview(carerId, serviceId)
                                // If not creates the review:
                                if(!existsReview){
                                    // 2.1. Si no existe iniciamos una nueva actividad/pop-up para opinar:
                                    if(author != null){
                                        showReviewSheet(context, author, serviceId, service)
                                    }
                                    // TODO - Recalculate carer's rating based on the fresh rating and the total
                                }else{
                                    // 2.2. Si ya existe una reseña lo mostramos con un sencillo toast:
                                    Toast.makeText(context,"Ya has dejado una opinión.",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        // https://stackoverflow.com/questions/35617468/how-to-use-bottomsheetdialog
        private fun showReviewSheet(context: Context, author: String, serviceId: String, service: Service?){
            val reviewSheetDialog = BottomSheetDialog(context)
            val viewSheet = LayoutInflater.from(context).inflate(R.layout.review_sheet, null)
            reviewSheetDialog.setContentView(viewSheet)

            var ownerReview: TextView = viewSheet.findViewById(R.id.review_text)
            var starOne: ImageView = viewSheet.findViewById(R.id.first_star)
            var starTwo: ImageView = viewSheet.findViewById(R.id.second_star)
            var starThree: ImageView = viewSheet.findViewById(R.id.third_star)
            var starFour: ImageView = viewSheet.findViewById(R.id.fourth_star)
            var starFive: ImageView = viewSheet.findViewById(R.id.fifth_star)
            var buttonSend: Button = viewSheet.findViewById(R.id.button_send)

            reviewSheetDialog.show()

            var serviceStars = 1
            var serviceReview = ""

            starOne.setOnClickListener {
                starTwo.setImageResource(R.drawable.empty_star)
                starThree.setImageResource(R.drawable.empty_star)
                starFour.setImageResource(R.drawable.empty_star)
                starFive.setImageResource(R.drawable.empty_star)
                serviceStars = 1
            }

            starTwo.setOnClickListener {
                starTwo.setImageResource(R.drawable.rating_star)
                starThree.setImageResource(R.drawable.empty_star)
                starFour.setImageResource(R.drawable.empty_star)
                starFive.setImageResource(R.drawable.empty_star)
                serviceStars = 2
            }

            starThree.setOnClickListener {
                starTwo.setImageResource(R.drawable.rating_star)
                starThree.setImageResource(R.drawable.rating_star)
                starFour.setImageResource(R.drawable.empty_star)
                starFive.setImageResource(R.drawable.empty_star)
                serviceStars = 3
            }

            starFour.setOnClickListener {
                starTwo.setImageResource(R.drawable.rating_star)
                starThree.setImageResource(R.drawable.rating_star)
                starFour.setImageResource(R.drawable.rating_star)
                starFive.setImageResource(R.drawable.empty_star)
                serviceStars = 4
            }

            starFive.setOnClickListener {
                starTwo.setImageResource(R.drawable.rating_star)
                starThree.setImageResource(R.drawable.rating_star)
                starFour.setImageResource(R.drawable.rating_star)
                starFive.setImageResource(R.drawable.rating_star)
                serviceStars = 5
            }

            buttonSend.setOnClickListener {
                Toast.makeText(context,"Usuario da $serviceStars al servicio",Toast.LENGTH_SHORT).show()
                val carerId = service?.carerUid
                val rating = serviceStars
                if(ownerReview.text.isNotEmpty()){
                    serviceReview = ownerReview.text.toString()
                }
                val opinion = serviceReview
                val review = Review(author,rating,opinion)

                if(carerId != null){
                    // TODO - Insertar review con asincronía:
                    CoroutineScope(Dispatchers.Main).launch {
                        FirebaseDatabaseModel.createCarerReview(carerId, serviceId, review)
                        // Recalculamos el rating:
                        FirebaseDatabaseModel.updateCarerRatings(carerId)
                    }

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
        holder.bindService(serviceId, service, userRole, serviceType)
    }

}
