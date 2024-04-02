package com.paco.mascocuida.activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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

    private lateinit var profilePics: ViewPager
    private lateinit var defaultPic: ImageView
    private lateinit var buttonAdd: FloatingActionButton
    private lateinit var profileName: TextView
    private lateinit var profileStars: TextView
    private lateinit var profileBio: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAction: Button
    private lateinit var user: FirebaseUser
    private lateinit var userUid: String
    private lateinit var picUri: Uri

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

        user = Firebase.auth.currentUser!!
        userUid = user.uid.toString()

        picUri = Uri.EMPTY

        profilePics = findViewById(R.id.viewpager_profile)
        buttonAdd = findViewById(R.id.float_addpics)
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
            // Función que se encarga de la edición:
            profileIsEditable()

        // Si existe es que es un dueño "cotilleando"
        }else{

            // TODO - Declarar, si hace falta, algún campo como "read only"
            extractUserInfo(existentCarerUid)
            profileIsReadOnly(existentCarerUid)
        }
    }

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


        builder.setView(view)

        val popUp = builder.create()




        buttonAction.setOnClickListener {
            // TODO - Pantalla de solicitud de cuidado - Activity - Nos tenemos que llevar el UID que llevamos con el intent:
            popUp.show()

            var selectedPet: Pet? = null

            spinnerPets.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedPet = spinnerPets.selectedItem as Pet
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

            selectedDate.setOnClickListener{
                showDatePickerDialog()
            }

            selectedTime.setOnClickListener {
                showTimePickerDialog()
            }



            // TODO - Button listener
            buttonRequest.setOnClickListener {

                if (checkFields(selectedPet,selectedDate.text.toString(),selectedTime.text.toString())){

                    //val date = SimpleDateFormat("dd-MM-yyyy").parse(selectedDate.toString())
                    val date = selectedDate.text.toString()
                    //val serviceDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
                    val time = selectedTime.text.toString()
                    //val serviceTime = SimpleDateFormat("HH:mm",Locale.getDefault()).format(time)
                    val requestInformation = aboutRequest.text.toString()


                    // TODO - Crear solicitud con los datos de la mascota, la fecha, la hora, los comentarios y un estatus predeterminado
                    val newService = Service(userUid,existentCarerUid,selectedPet,date,time,requestInformation,"pending")

                    CoroutineScope(Dispatchers.Main).launch {
                        // Creamos un nuevo UID para el servicio:
                        val serviceUid = UUID.randomUUID().toString()

                        FirebaseDatabaseModel.addService(serviceUid,newService)
                    }
                }

            }


        }
    }

    private fun checkFields(pet: Pet?, date: String, time: String): Boolean{
        return pet != null && date.isNotEmpty() && time.isNotEmpty()
    }

    private fun showDatePickerDialog(){
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun onDateSelected(day: Int, month: Int, year: Int){
        selectedDate.setText("$day/$month/$year")
    }

    private fun showTimePickerDialog(){
        val timePicker = TimePickerFragment { onTimeSelected(it)}
        timePicker.show(supportFragmentManager, "timePicker")
    }

    private fun onTimeSelected(time: String){
        selectedTime.setText(time)
    }

    private fun profileIsEditable(){

        // El botón hace otra función
        buttonAction.text = "Guardar cambios"

        // Se podrá ver el botón de añadir fotos al perfil:
        buttonAdd.visibility = View.VISIBLE

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

        buttonAdd.setOnClickListener{
            // Llamamos a la Activity según lo dispuesto en la documentación oficial de Android:
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

        }

        buttonAction.setOnClickListener {
            //TODO: Save changes
        }
    }

    private fun makeToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    // Función que extrae un objeto que representa al usuario con el UID que le hemos pasado como parámetro:
    private fun extractUserInfo(userUid: String){
        // Lanzamos una corrutina (Asincronía de Firebase):
        CoroutineScope(Dispatchers.Main).launch {

            // Extraemos el objeto de usuario:
            val profileUser = FirebaseDatabaseModel.getCarerFromFirebase(userUid)

            if(profileUser != null){

                // Accedemos al campo "pics". Si no es nulo o vacío llenaremos nuestro Pager con sus imágenes de perfil con el adaptador:
                if(!profileUser.getPics().isNullOrEmpty()){

                    CoroutineScope(Dispatchers.Main).launch {
                        val imagesMap = FirebaseDatabaseModel.listCarerPics(userUid)
                        val imagesAdapter = ImageAdapter(imagesMap)
                        profilePics.adapter = imagesAdapter

                        for((key,value) in imagesMap){                                                      // DEBUG TODO - Delete
                            Log.d("ImagesMapContent","Key: $key / Url: $value")                   //
                        }                                                                                  // TODO - Delete
                    }
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
                    profileStars.text = "0/5"
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