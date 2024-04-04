package com.paco.mascocuida.activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R
import com.paco.mascocuida.adapters.ImageAdapter
import com.paco.mascocuida.adapters.PetsAdapter
import com.paco.mascocuida.data.Pet
import com.paco.mascocuida.data.Service
import com.paco.mascocuida.fragments.DatePickerFragment
import com.paco.mascocuida.fragments.TimePickerFragment
import com.paco.mascocuida.models.FirebaseDatabaseModel
import com.paco.mascocuida.models.FirebaseStorageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    // Variables que controlan la interfaz del perfil del cuidador:
    private lateinit var profilePics: ViewPager
    private lateinit var defaultPic: ImageView
    private lateinit var imagesMap: HashMap<String,String>
    private lateinit var buttonAdd: FloatingActionButton
    private lateinit var buttonRemove: FloatingActionButton
    private lateinit var profileName: TextView
    private lateinit var profileStars: TextView
    private lateinit var profileBio: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAction: Button
    private lateinit var user: FirebaseUser
    private lateinit var userUid: String
    private lateinit var picUri: Uri

    // Variables que controlan la solicitud de cuidado al cuidador:
    private lateinit var spinnerPets: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<Pet>
    private lateinit var selectedDate: EditText
    private lateinit var selectedTime: EditText
    private lateinit var aboutRequest: EditText
    private lateinit var buttonRequest: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Accedemos al usuario actual:
        user = Firebase.auth.currentUser!!
        userUid = user.uid.toString()

        picUri = Uri.EMPTY

        // Asignamos vistas:
        profilePics = findViewById(R.id.viewpager_profile)
        buttonAdd = findViewById(R.id.float_addpics)
        buttonRemove = findViewById(R.id.float_removepic)
        profileName = findViewById(R.id.textview_username)
        profileStars = findViewById(R.id.textview_stars)
        profileBio = findViewById(R.id.text_bio)
        recyclerView = findViewById(R.id.recycler_reviews)
        buttonAction = findViewById(R.id.button_action)

        // Extraemos el UID del cuidador del extra del intent:
        val existentCarerUid = intent.getStringExtra("carerUid")

        // Si no existe es que es el propio usuario el que está accediendo a su perfil:
        if(existentCarerUid.isNullOrEmpty()){
            // Tomamos el userUid del Auth como referencia para mostrar el perfil (el propio usuario):
            extractUserInfo(userUid)
            // El perfil será editable, lo controlamos con una función modular:
            profileIsEditable()
        // Si existe es que es un dueño que quiere contratar los servicios del cuidador:
        }else{
            // Tomamos el userUid que hemos pasado del intent para mostrar el perfil (el cuidador elegido):
            extractUserInfo(existentCarerUid)
            // El perfil sólo será consultable, lo controlamos con otra función modular:
            profileIsReadOnly(existentCarerUid)
        }
    }

    // Función que hace que el perfil sea de sólo lectura (esta actividad la está viendo un dueño):
    private fun profileIsReadOnly(existentCarerUid: String){
        // La biografía no se puede cambiar:
        profileBio.isEnabled = false
        // El botón hace otra función
        buttonAction.text = "Solicitar cuidado"

        // Inflar la vista de solicitud:
        val builder = AlertDialog.Builder(this).setCancelable(true)
        val view = layoutInflater.inflate(R.layout.request_service, null)
        buttonRequest = view.findViewById(R.id.button_request)
        spinnerPets = view.findViewById(R.id.spinner_request)
        selectedDate = view.findViewById(R.id.edit_date)
        selectedTime = view.findViewById(R.id.edit_time)
        aboutRequest = view.findViewById(R.id.request_text)

        // Accedemos al listado de mascotas del dueño:
        CoroutineScope(Dispatchers.Main).launch {
            // Sacamos el HashMap:
            val petsMap = FirebaseDatabaseModel.listPets(userUid)
            // Lo convertimos a una lista:
            val petsList = petsMap.values.toList()
            spinnerAdapter = ArrayAdapter(this@ProfileActivity,android.R.layout.simple_list_item_1,petsList)
            spinnerPets.adapter = spinnerAdapter
        }

        // Asignamos la lista y preparamos el pop-up de solicitud:
        builder.setView(view)
        val popUp = builder.create()

        // El dueño pulsa en el botón de solicitar cuidado:
        buttonAction.setOnClickListener {
            // Mostramos el pop-up:
            popUp.show()

            var selectedPet: Pet? = null
            // Extraemos la mascota seleccionada del listado del dueño (basado parcialmente en https://stackoverflow.com/a/56590676)
            spinnerPets.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedPet = spinnerPets.selectedItem as Pet
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

            // Invocamos los métodos para mostrar los fragments de fecha y hora al pulsar sobre ellos:
            selectedDate.setOnClickListener{
                showDatePickerDialog()
            }

            selectedTime.setOnClickListener {
                showTimePickerDialog()
            }

            // Listener del botón de enviar solicitud:
            buttonRequest.setOnClickListener {

                // Comprobamos que estén todos los campos básicos con el método de comprobación:
                if (checkFields(selectedPet,selectedDate.text.toString(),selectedTime.text.toString())){

                    // Inicializamos las variables que pasaremos al constructor:
                    val date = selectedDate.text.toString()
                    val time = selectedTime.text.toString()
                    val requestInformation = aboutRequest.text.toString()

                    // Construimos un nuevo objeto de cuidado con todos los datos y un estatus pendiente:
                    val newService = Service(userUid, existentCarerUid, selectedPet, date,
                        time, requestInformation, "pending")

                    CoroutineScope(Dispatchers.Main).launch {
                        // Creamos un nuevo UID para el servicio:
                        val serviceUid = UUID.randomUUID().toString()
                        // Lo añadimos a la base de datos:
                        FirebaseDatabaseModel.addService(serviceUid, newService)
                        // Toast:
                        Toast.makeText(this@ProfileActivity,"Se ha enviado la solicitud",Toast.LENGTH_SHORT).show()
                        // Cerramos el popup:
                        popUp.dismiss()
                    }
                }
            }
        }
    }

    // Función que comprueba los campos pasados como parámetros (si tienen algo):
    private fun checkFields(pet: Pet?, date: String, time: String): Boolean{
        return pet != null && date.isNotEmpty() && time.isNotEmpty()
    }

    // Función que invoca el fragmento para elegir la fecha:
    private fun showDatePickerDialog(){
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(supportFragmentManager, "datePicker")
    }

    // Función que asigna la fecha seleccionada por el usuario en el TextView:
    private fun onDateSelected(day: Int, month: Int, year: Int){
        selectedDate.setText("$day/$month/$year")
    }

    // Función que invoca el fragmento para elegir la hora:
    private fun showTimePickerDialog(){
        val timePicker = TimePickerFragment { onTimeSelected(it)}
        timePicker.show(supportFragmentManager, "timePicker")
    }

    // Función que asigna la hora seleccionada por el usuario en el TextView:
    private fun onTimeSelected(time: String){
        selectedTime.setText(time)
    }

    // Función que se encarga de que el perfil sea editable (es el propio cuidador):
    private fun profileIsEditable(){

        // El botón hace la función de guardar los cambios:
        buttonAction.text = "Guardar cambios"

        // TODO - Comprobar si el imagesMap tiene algo dentro, ya que si no hay nada el botón de eliminar crashea la aplicación
        if(::imagesMap.isInitialized && imagesMap.isNotEmpty()){

        }
        buttonRemove.visibility = View.VISIBLE

        // Se podrá ver el botón de añadir fotos al perfil:
        buttonAdd.visibility = View.VISIBLE

        // Para subir una foto:
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                // Se intentó utilizar "ImageDecoder" pero requiere un nivel de API superior. Se usa una solución
                // más antigua, obsoleta pero válida:
                // Inicializamos la variable de la imagen de perfil con la URI que selecciona el usuario:
                val picBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                // Para comprimir y ahorrar datos seguimos: https://stackoverflow.com/a/43885809
                val baos = ByteArrayOutputStream()
                picBitmap.compress(Bitmap.CompressFormat.JPEG,25, baos)

                // Actualizamos el valor de picUri para que apunte a la imagen seleccionada:
                picUri = uri

                // Guardamos la imagen en la carpeta que le toca al usuario:
                CoroutineScope(Dispatchers.Main).launch {

                    val picUrl = FirebaseStorageModel.uploadCarerPic(userUid,picUri)

                    if(picUrl != null) {
                        FirebaseDatabaseModel.addCarerPic(userUid, picUrl)
                    }

                    makeToast("Imagen guardada en tu perfil")
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        // TODO - Remove profile pics
        // First get the button on a listener
        // Then extract the information from the displayed image (picUrl).
        // Delete the image reference from the database
        // Finally delete the actual image from Firebase storage (FirebaseStorageModel)

        buttonRemove.setOnClickListener{
            // Obtenemos la posición de la imagen que está siendo exhibida
            val currentPosition = profilePics.currentItem

            // Creamos una lista de id's de imágenes con las claves de nuestro HashMap:
            val idList = ArrayList(imagesMap.keys)

            // Si la posición actual está dentro de los límites lógicos y de la lista:
            if (currentPosition >= 0 && currentPosition < idList.size){
                val imageId = idList[currentPosition]
                val imageUrl = imagesMap[imageId]
                // Sacamos el valor de la ID de la imagen y de la URL para borrarla en la BD y en el Storage:
                if(imageId != null && imageUrl != null){
                    FirebaseDatabaseModel.removeCarerPic(userUid,imageId)
                    FirebaseStorageModel.removeCarerPic(imageUrl)
                }
            }
        }

        buttonAdd.setOnClickListener{
            // Comprobamos que el usuario no tenga más de cinco fotos:
            if(::imagesMap.isInitialized){
                if(imagesMap.size < 5){
                    // Llamamos a la Activity según lo dispuesto en la documentación oficial de Android:
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }else{
                    makeToast("Ya has subido muchas fotos, borra alguna")
                }
            }else{
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

        }

        buttonAction.setOnClickListener {
            //TODO: Save changes

        }
    }

    // Función que hace toasts de forma modular:
    private fun makeToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    // Función que extrae un objeto que representa al usuario con el UID que le hemos pasado como parámetro:
    private fun extractUserInfo(userUid: String){
        // Lanzamos una corrutina:
        CoroutineScope(Dispatchers.Main).launch {
            // Extraemos el objeto de usuario:
            val profileUser = FirebaseDatabaseModel.getCarerFromFirebase(userUid)

            if(profileUser != null){
                // Accedemos al campo "pics". Si no es nulo o vacío llenaremos nuestro Pager con sus imágenes de perfil con el adaptador:
                if(!profileUser.getPics().isNullOrEmpty()){
                    CoroutineScope(Dispatchers.Main).launch {
                        imagesMap = FirebaseDatabaseModel.listCarerPics(userUid)
                        val imagesAdapter = ImageAdapter(imagesMap)
                        profilePics.adapter = imagesAdapter
                    }
                    // Si el usuario aún no tiene imágenes dejaremos como visible una imagen predefinida:
                }else{
                    defaultPic = findViewById(R.id.imageview_default)
                    defaultPic.visibility = View.VISIBLE
                }

                // Accedemos al nombre, apellidos, rating, biografía y las reviews, si tuviese:
                val userFullName = profileUser.getName() + " " + profileUser.getLastname()
                profileName.text = userFullName

                val userRating = profileUser.getRating()
                val formattedRating = String.format("%.2f", userRating)
                if(userRating == null){
                    profileStars.text = "-"
                }else{
                    profileStars.text = formattedRating + "/5"
                }
                val aboutMe = profileUser.getAboutMe()
                profileBio.setText(aboutMe)

                // TODO : Async call for reviews
                if(!profileUser.getReviews().isNullOrEmpty()){

                    /*CoroutineScope(Dispatchers.Main).launch {

                    }*/
                }

            }
        }
    }
    
}