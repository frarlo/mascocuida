package com.paco.mascocuida.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Owner
import com.paco.mascocuida.data.Review
import com.paco.mascocuida.data.Service
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
* Esta clase es un adaptador que nos permite mostrar al usuario, en este caso específico, un listado de Servicios en una vista
* de RecyclerView. Según el rol que le pasemos y el estatus del servicio. Dado que el nucleo de nuestra aplicación es ofrecer
* servicios de cuidado, este adaptador es el más complejo y el que más lógica implementa.
*/
class ServicesAdapter(private val servicesMap: Map<String, Service>, private val userRole: String,
    private val serviceStatus: String): RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {

    // Clase principal del Adaptador. Representa cada item dentro de la lista (o Mapa):
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // Inicializamos todos los elementos de la vista y declaramos el contexto en caso de necesitarlo:
        private var linearLayoutOwner: LinearLayout = view.findViewById(R.id.linear_owner)
        private var userFullname: TextView = view.findViewById(R.id.text_userfullname)
        private var userPetName: TextView = view.findViewById(R.id.text_userpet)
        private var buttonPetInfo: ImageButton = view.findViewById(R.id.button_petinfo)
        private var linearLayoutInformation: LinearLayout = view.findViewById(R.id.linear_information)
        private var serviceInformation: TextView = view.findViewById(R.id.text_serviceinfo)
        private var serviceFullDate: TextView = view.findViewById(R.id.text_date)
        private var buttonDecline: Button = view.findViewById(R.id.button_decline)
        private var buttonAccept: Button = view.findViewById(R.id.button_accept)
        private val context = view.context                                             // Needed if we want to start a new activity.

        // Este método bindea el objeto a la vista:
        @SuppressLint("SetTextI18n") // Reason: Fullname concatenation should not be subjected to Translation - frarlo
        fun bindService(serviceId: String, service: Service?, userRole: String, serviceStatus: String) {

            var userObject: Owner? = null

            // Lanzamos una corrutina asíncrona para recuperar toda la información del servicio:
            CoroutineScope(Dispatchers.Main).launch{
                userObject = FirebaseDatabaseModel.getOwnerFromFirebase(service?.ownerUid)
                userFullname.text = userObject?.getName() + " " + userObject?.getLastname()
                userPetName.text = service?.pet?.getName()
                serviceInformation.text = service?.information
                serviceFullDate.text = "El día " + service?.date + " a las " + service?.time      // Check for I18n alternatives
            }

            // Inicializamos e inflamos una vista emergente para manejar las distintas opciones que pueden pulsarse según lo visto:
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

            // Dependiendo del rol del usuario y del estado del servicio, los botones
            // y la visibilidad de algunos elementos cambiarán:
            if(userRole == "Carer"){
                // Según el estatus del servicio...
                when(serviceStatus){
                    "pending" -> {
                        // No cambia la visibilidad de ningún botón
                    }
                    "accepted" -> {
                        // El botón de rechazar desaparece y cambia el nombre del botón aceptar:
                        buttonDecline.visibility = View.GONE
                        // buttonAccept.text = "Información"
                        buttonAccept.visibility = View.GONE
                    }
                    "completed" -> {
                        // Se elimina la visibilidad de la información y de los botones.
                        linearLayoutInformation.visibility = View.GONE
                        buttonDecline.visibility = View.GONE
                        buttonAccept.visibility = View.GONE
                    }
                    "rejected" -> {
                        buttonDecline.visibility = View.GONE
                        buttonAccept.visibility = View.GONE
                    }
                }

            }else if(userRole == "Owner"){
                // Quitamos la visibilidad de la información del propio dueño (es irrelevante en este rol):
                linearLayoutOwner.visibility = View.GONE
                when(serviceStatus){
                    "pending" -> {
                        // La visibilidad de aceptar desaparece, ya que como mucho el dueño puede cancelar la solicitud:
                        buttonAccept.visibility = View.GONE
                        buttonDecline.text = "Cancelar solicitud"
                    }
                    "accepted" -> {
                        // Cambia el contenido de los botones:
                        buttonDecline.text = "Finalizar servicio"
                        //buttonAccept.text = "Información"
                        buttonAccept.visibility = View.GONE
                    }
                    "completed" -> {
                        // La visibilidad de rechazar desaparece, ya que no hay nada que "completar" o "cancelar" ya:
                        buttonDecline.visibility = View.GONE
                        // También se omite la información de la solicitud y se cambia el texto del botón:
                        linearLayoutInformation.visibility = View.GONE
                        buttonAccept.text = "Opina sobre el cuidado"
                    }
                    "rejected" -> {

                    }
                }
            }

            // Listener del botón de más información de la mascota - Muestra un diálogo con su información
            buttonPetInfo.setOnClickListener {
                showPetDialog(context,service)
            }

            // Listener del botón de rechazo:
            buttonDecline.setOnClickListener {

                // Si el usuario es cuidador...
                if(userRole == "Carer"){

                    // Muestra el popup personalizado y hacemos listen sobre sus botones
                    popHeader.text = "¿Quieres rechazar la petición?"
                    popSubHeader.text = "Ya no podrás aceptarla"
                    popUp.show()

                    buttonRight.setOnClickListener {
                        // Actualiza el estatus del servicio en la base de datos:
                        FirebaseDatabaseModel.updateServiceStatus(serviceId,"rejected")
                        Toast.makeText(context,"Se ha rechazado la solicitud",Toast.LENGTH_SHORT).show()
                        popUp.dismiss()
                    }
                    buttonLeft.setOnClickListener {
                        // El usuario no quiere hacer nada, cerramos el popup
                        popUp.dismiss()
                    }
                }

                // Si el usuario es dueño...
                if(userRole == "Owner"){

                    // Y el cuidado está en fase "aceptado". Podrá finalizarlo ya si quiere:
                    if(serviceStatus == "accepted"){
                        popHeader.text = "¿Quieres finalizar el servicio?"
                        popSubHeader.text = "El servicio pasará a estar acabado."
                        popUp.show()

                        buttonRight.setOnClickListener {
                            // Actualiza el estatus del servicio en la base de datos:
                            FirebaseDatabaseModel.updateServiceStatus(serviceId,"completed")
                            popUp.dismiss()
                        }
                        buttonLeft.setOnClickListener {
                            popUp.dismiss()
                        }

                    // Si no está en esta fase, será que aún está "pending". Podrá cancelar la petición:
                    }else{
                        popHeader.text = "¿Quieres cancelar la petición?"
                        popSubHeader.text = "Tendrás que realizarla de nuevo"
                        popUp.show()

                        buttonRight.setOnClickListener {
                            // Eliminamos el servicio:
                            FirebaseDatabaseModel.removeService(serviceId)
                            popUp.dismiss()
                        }
                        buttonLeft.setOnClickListener {
                            popUp.dismiss()
                        }
                    }
                }
            }

            // Listener del botón de aceptar:
            buttonAccept.setOnClickListener {

                // Si el usuario es cuidador:
                if(userRole == "Carer"){

                    // Si el estatus está aceptado podrá pedir más información:
                    if(serviceStatus == "accepted"){
                        // TODO: Delete toast - DO Chat Activity if there is enough time:
                        Toast.makeText(context,"Carer quiere información",Toast.LENGTH_SHORT).show()

                    // Si está en pendiente podrá aceptarla:
                    }else if(serviceStatus == "pending"){
                        popHeader.text = "¿Quieres aceptar la petición?"
                        popSubHeader.text = "Ya no podrás rechazarla"
                        popUp.show()

                        buttonRight.setOnClickListener {
                            // Actualizamos la base de datos con el nuevo estatus del servicio:
                            FirebaseDatabaseModel.updateServiceStatus(serviceId,"accepted")
                            Toast.makeText(context,"Solicitud aceptada", Toast.LENGTH_SHORT).show()
                            popUp.dismiss()
                        }
                        buttonLeft.setOnClickListener {
                            popUp.dismiss()
                        }
                    }
                }

                // Si el usuario es el dueño:
                if(userRole == "Owner"){

                    // Si el estatus está aceptado podrá pedir más información:
                    if(serviceStatus == "accepted"){
                        // TODO: Delete toast - DO Chat Activity if there is enough time:
                        Toast.makeText(context,"Owner quiere información",Toast.LENGTH_SHORT).show()

                    // Si el estatus está completado...
                    }else if(serviceStatus == "completed"){

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

        // Función que muestra en un simple emergente toda la información de la mascota que está incluida en la solicitud:
        private fun showPetDialog(context: Context, service: Service?){

            // Construimos e inflamos la vista:
            val builder = AlertDialog.Builder(context).setCancelable(true)
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.pet_sheet,null)

            // Declaramos e inicializamos todos los elementos de la misma:
            val petName: TextView = view.findViewById(R.id.sheet_pet_name)
            val petSpecies: TextView = view.findViewById(R.id.sheet_pet_species)
            val petSize: TextView = view.findViewById(R.id.sheet_pet_size)
            val petGender: TextView = view.findViewById(R.id.sheet_pet_gender)
            val isSterilised: CheckBox = view.findViewById(R.id.sheet_pet_sterilised)
            val likesDogs: CheckBox = view.findViewById(R.id.sheet_pet_likesdogs)
            val likesCats: CheckBox = view.findViewById(R.id.sheet_pet_likescats)
            val buttonBack: Button = view.findViewById(R.id.sheet_pet_buttonback)

            builder.setView(view)
            val popUp = builder.create()

            // Extraemos el objeto tipo mascota de la solicitud de servicio:
            val petObject = service?.pet

            // Llenamos la vista con los datos de la mascota si no es nula:
            if(petObject != null){
                petName.text = petObject.getName()
                petSpecies.text = petObject.getSpecies()
                petSize.text = petObject.getSize()
                petGender.text = petObject.getGender()
                if(petObject.getIsSterilised() == true){
                    isSterilised.isChecked = true
                }
                if(petObject.getLikesDogs() == true){
                    likesDogs.isChecked = true
                }
                if(petObject.getLikesCats() == true){
                    likesCats.isChecked = true
                }

                // Finalmente mostramos la vista:
                popUp.show()

                // Simple listener del botón para volver atrás (cerrar popUp):
                buttonBack.setOnClickListener {
                    popUp.dismiss()
                }
            }
        }

        // Método que muestra un BottomSheet para dejar la opinión sobre un servicio de cuidado según lo visto:
        // https://stackoverflow.com/questions/35617468/how-to-use-bottomsheetdialog
        private fun showReviewSheet(context: Context, author: String, serviceId: String, service: Service?){

            // Creamos e inflamos la vista:
            val reviewSheetDialog = BottomSheetDialog(context)
            val viewSheet = LayoutInflater.from(context).inflate(R.layout.review_sheet, null)
            reviewSheetDialog.setContentView(viewSheet)

            // Inicializamos los elementos de la vista:
            val ownerReview: TextView = viewSheet.findViewById(R.id.review_text)
            val starOne: ImageView = viewSheet.findViewById(R.id.first_star)
            val starTwo: ImageView = viewSheet.findViewById(R.id.second_star)
            val starThree: ImageView = viewSheet.findViewById(R.id.third_star)
            val starFour: ImageView = viewSheet.findViewById(R.id.fourth_star)
            val starFive: ImageView = viewSheet.findViewById(R.id.fifth_star)
            val buttonSend: Button = viewSheet.findViewById(R.id.button_send)

            // Mostramos el BottomSheet:
            reviewSheetDialog.show()

            // Inicializamos tanto la puntuación como la cadena de opinión:
            var serviceStars = 1
            var serviceReview = ""

            // Según la estrella que pulse el usuario (de izquierda a derecha) el servicio tendrá más o menos puntuación
            // Cambiamos dinámicamente tanto la variable de puntuación como el recurso de imagen para añadir o quitar estrellas:

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

            // Listener del botón de enviar opinión:
            buttonSend.setOnClickListener {

                // Extraemos el ID del cuidador y la puntuación:
                val carerId = service?.carerUid
                val rating = serviceStars

                // Comprobamos que no este vacío el campo de la opinión para que no salte una excepción:
                if(ownerReview.text.isNotEmpty()){
                    serviceReview = ownerReview.text.toString()
                }
                // Declaramos el texto de la opinión y creamos un objeto tipo review:
                val opinion = serviceReview
                val review = Review(author,rating,opinion)

                // Comprobamos que el ID del cuidador no sea nulo y que la opinión no tenga más de 150 caracteres:
                if(carerId != null && opinion.length <= 150){

                    // Lanzamos una corrutina para incluir la opinión en el documento del cuidador y recalcular su puntuación media:
                    CoroutineScope(Dispatchers.Main).launch {
                        FirebaseDatabaseModel.createCarerReview(carerId, serviceId, review)
                        FirebaseDatabaseModel.updateCarerRatings(carerId)

                        // Cerramos el pop-up y mostramos un mensaje de confirmación:
                        reviewSheetDialog.dismiss()
                        Toast.makeText(context, "Tu opinión ha sido publicada", Toast.LENGTH_SHORT).show()
                    }

                // Si la opinión tiene más caracteres de los permitidos mostramos un toast y no persistimos el objeto:
                }else{
                    Toast.makeText(context,"La opinión es demasiado larga",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Método predeterminado del Adaptador que retorna la vista creada por el adaptador:
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.service_layout, parent, false)
        return ViewHolder(view)
    }

    // Método predeterminado del Adaptador que retorna el tamaño del HashMap de Servicios:
    override fun getItemCount(): Int {
        return servicesMap.size
    }

    // Método predeterminado que bindeea cada item a la vista llamando al método:
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val serviceId = servicesMap.keys.toList()[position]
        val service = servicesMap[serviceId]
        holder.bindService(serviceId, service, userRole, serviceStatus)
    }

}
