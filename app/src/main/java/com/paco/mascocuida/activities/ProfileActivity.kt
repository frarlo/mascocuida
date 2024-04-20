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
import android.widget.LinearLayout
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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R
import com.paco.mascocuida.adapters.ImageAdapter
import com.paco.mascocuida.adapters.ReviewsAdapter
import com.paco.mascocuida.data.Carer
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
import java.util.UUID

/* Esta actividad maneja la visualización de perfiles de cuidador. Es una de las más importantes y se reutiliza según
*  si el usuario es un dueño o un cuidador. Si es un dueño la Actividad vendrá con un putExtra en el que se especificará
*  el UID del cuidador a ver, la vista será solo de comprobación y el botón permitirá realizar una solicitud de cuidado.
*  Por otra parte, si el usuario que llega es el propio cuidador, que quiere ver o editar su perfil, la vista será en "modo
*  de edición" y activa botones adicionales y cambia la funcionalidad del botón principal, donde un sencillo SheetView permite
*  cambiar al Cuidador sus datos personales.
*/
class ProfileActivity : AppCompatActivity() {

    // Variables que controlan la interfaz del perfil del cuidador:
    private lateinit var profilePics: ViewPager
    private lateinit var defaultPic: ImageView
    private lateinit var imagesMap: HashMap<String,String>
    private lateinit var buttonAdd: FloatingActionButton
    private lateinit var buttonRemove: FloatingActionButton
    private lateinit var profileName: TextView
    private lateinit var profileLocation: TextView
    private lateinit var profileStars: TextView
    private lateinit var profileBio: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAction: Button
    private lateinit var user: FirebaseUser
    private lateinit var userUid: String
    private lateinit var picUri: Uri
    private lateinit var profileUser: Carer
    private var imagesMapIsInitialized = false
    private var userCanSeeButtons = false

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
        profileLocation = findViewById(R.id.textview_location)
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

            // El usuario puede editar las fotos (es el dueño):
            userCanSeeButtons = true

            // El perfil será editable, lo controlamos con una función modular:
            profileIsEditable()

        // Si existe el extraIntent es que es un dueño que quiere contratar los servicios del cuidador:
        }else{

            // Tomamos el userUid que hemos pasado del intent para mostrar el perfil (el cuidador elegido):
            extractUserInfo(existentCarerUid)

            // El perfil sólo será consultable, lo controlamos con otra función modular:
            profileIsReadOnly(existentCarerUid)

        }
    }

    // Función que hace que el perfil sea de sólo lectura (esta actividad la está viendo un dueño):
    private fun profileIsReadOnly(existentCarerUid: String){
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

            // Sacamos el HashMap de Mascotas:
            val petsMap = FirebaseDatabaseModel.listPets(userUid)

            // Lo convertimos a una lista:
            val petsList = petsMap.values.toList()

            // Pasamos al adaptador la lista de mascotas y luego asignamos el adaptador personalizado a nuestra vista:
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

            // Inicializamos la variable de mascota a nula:
            var selectedPet: Pet? = null

            // Extraemos la mascota seleccionada del listado del dueño (basado parcialmente en https://stackoverflow.com/a/56590676)
            spinnerPets.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedPet = spinnerPets.selectedItem as Pet
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}

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

                    // Lanzamos una corrutina para crear la solicitud de servicio de forma asíncrona:
                    CoroutineScope(Dispatchers.Main).launch {

                        // Creamos un nuevo UID para el servicio:
                        val serviceUid = UUID.randomUUID().toString()

                        // Lo añadimos a la base de datos:
                        FirebaseDatabaseModel.addService(serviceUid, newService)

                        // Toast:
                        makeToast("Se ha enviado la solicitud")

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
        buttonAction.text = "Editar mi información"

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
                        makeToast("Imagen subida, puede tardar un poco en aparecer")
                    }
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        // Listener del botón flotante para eliminar la foto que se está mostrando actualmente en el perfil:
        buttonRemove.setOnClickListener{

            // Obtenemos la posición de la imagen que está siendo exhibida
            val currentPosition = profilePics.currentItem

            // Creamos una lista de id's de imágenes con las claves de nuestro HashMap:
            val idList = ArrayList(imagesMap.keys)

            // Si la posición actual está dentro de los límites lógicos y de la lista:
            if (currentPosition >= 0 && currentPosition < idList.size){

                // Sacamos tanto el ID como la URL de la imagen:
                val imageId = idList[currentPosition]
                val imageUrl = imagesMap[imageId]

                // Si ambos valores no son nulos llamamos al método que se encarga del borrado de la imagen:
                if(imageId != null && imageUrl != null){
                    popUpDeletePhoto(imageId,imageUrl)
                }

            }
        }

        // Listener del otro botón flotante, el de añadir imágenes:
        buttonAdd.setOnClickListener{
            // Comprobamos que el usuario tenga el mapa de imágenes inicializado y que no tenga más de cinco fotos:
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

        // Listener del botón principal de la actividad, para editar la información personal del cuidador:
        buttonAction.setOnClickListener {
            editInformation()
        }
    }

    // Función que maneja la acción de borrar una imagen (cuyos valores ya se pasan como parámetros) y que hace
    // saltar un pop-up de advertencia:
    private fun popUpDeletePhoto(imageId: String, imageUrl: String){

        // Inflado del pop-up de advertencia:
        val builder = AlertDialog.Builder(this).setCancelable(true)
        val view = layoutInflater.inflate(R.layout.reusable_popup, null)
        val buttonNo = view.findViewById<Button>(R.id.button_pop_left)
        val buttonYes = view.findViewById<Button>(R.id.button_pop_right)
        val textPop = view.findViewById<TextView>(R.id.pop_up_header)
        val subTextPop = view.findViewById<TextView>(R.id.pop_up_subheader)
        buttonNo.text = "Cancelar"
        buttonYes.text = "Borrar"
        textPop.text = "¿Quieres borrar esta imagen de perfil?"
        subTextPop.text = "Esta acción la borrará y no podrás recuperarla"

        // Lo construimos y lo mostramos:
        builder.setView(view)
        val popUp = builder.create()
        popUp.show()

        // Si dice no el pop-up se cierra y no pasa nada:
        buttonNo.setOnClickListener {
            popUp.dismiss()
        }

        // En caso contrario, el usuario confirma que quiere eliminar esa imagen...
        buttonYes.setOnClickListener {

            // Llamamos primero al método de borrar la referencia de la imagen del documento de la base de datos:
            FirebaseDatabaseModel.removeCarerPic(userUid, imageId)
            // Y luego la eliminamos la imagen guardada en el almacenamiento de Firebase:
            FirebaseStorageModel.removeCarerPic(imageUrl)


            makeToast("La imagen se ha eliminado")
            popUp.dismiss()

            // Refrescamos vista de imágenes:
            extractUserPics(userUid)
        }
    }

    // Función que hace toasts de forma modular:
    private fun makeToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    // Función que extrae las imágenes de perfil de un cuidador pasándole UID del cuidador como parámetro:
    private fun extractUserPics(userUid: String){

        // Si el HashMap no es nulo o está vacío lanzamos una corrutina para extraer el mapa de imágenes del cuidador:
        if(!profileUser.getPics().isNullOrEmpty()){
            CoroutineScope(Dispatchers.Main).launch {
                imagesMap = FirebaseDatabaseModel.listCarerPics(userUid)
                val imagesAdapter = ImageAdapter(imagesMap)
                profilePics.adapter = imagesAdapter

                // Cambiamos la visibilidad de los botones:
                imagesMapIsInitialized = true
                if(userCanSeeButtons){
                    changeButtonsVisibility()
                }
            }
        // Si el usuario aún no tiene imágenes dejaremos como visible una imagen predefinida:
        }else{
            defaultPic = findViewById(R.id.imageview_default)
            defaultPic.visibility = View.VISIBLE

            // La visibilidad de los botones también cambia en este caso:
            if(userCanSeeButtons){
                changeButtonsVisibility()
            }
        }
    }

    // Función que extrae un objeto que representa al usuario con el UID que le hemos pasado como parámetro:
    private fun extractUserInfo(userUid: String){
        // Lanzamos una corrutina:
        CoroutineScope(Dispatchers.Main).launch {

            // Extraemos el objeto de usuario:
            profileUser = FirebaseDatabaseModel.getCarerFromFirebase(userUid)!!

            // Llamamos al método para extraer las imágenes del perfil de cuidador:
            extractUserPics(userUid)

            // Accedemos al nombre, apellidos, rating, biografía y las reviews, si tuviese:
            val userFullName = profileUser.getName() + " " + profileUser.getLastname()
            profileName.text = userFullName
            profileLocation.text = profileUser.getLocation()

            val userRating = profileUser.getRating()
            val formattedRating = String.format("%.2f", userRating)
            if(userRating == null){
                profileStars.text = "-"
            }else{
                profileStars.text = formattedRating + "/5"
            }
            val aboutMe = profileUser.getAboutMe()
            profileBio.text = aboutMe

            if(!profileUser.getReviews().isNullOrEmpty()){
                // Las reviews van en un HashMap con su adaptador personalizado:
                CoroutineScope(Dispatchers.Main).launch {
                    val reviewsMap = FirebaseDatabaseModel.listCarerReviews(userUid)
                    val reviewsAdapter = ReviewsAdapter(reviewsMap)
                    recyclerView.adapter = reviewsAdapter
                }
            }

        }
    }

    // Función que abre un BottomSheet para editar los detalles del perfil:
    private fun editInformation(){

        // Inflamos la vista y la mostramos:
        val profileSheetDialog = BottomSheetDialog(this)
        val viewSheet = LayoutInflater.from(this).inflate(R.layout.info_sheet,null)
        profileSheetDialog.setContentView(viewSheet)
        profileSheetDialog.show()

        // Inicializamos los widgets de la vista con su constante:
        val editedName: EditText = viewSheet.findViewById(R.id.text_firstname)
        val editedLastname: EditText = viewSheet.findViewById(R.id.text_lastname)
        val editedLocation: EditText = viewSheet.findViewById(R.id.text_location)
        val linearAboutMe: LinearLayout = viewSheet.findViewById(R.id.layout_carer_bio)
        val editedAboutMe: EditText = viewSheet.findViewById(R.id.text_edit_bio)
        val buttonSave: Button = viewSheet.findViewById(R.id.button_save)

        // El usuario es un cuidador, por lo que habrá un campo más en el BottomSheet:
        linearAboutMe.isVisible = true

        // Rellenamos de forma predeterminada la vista de edición con los datos actuales:
        editedName.setText(profileUser.getName())
        editedLastname.setText(profileUser.getLastname())
        editedLocation.setText(profileUser.getLocation())
        editedAboutMe.setText(profileUser.getAboutMe())

        // Listener del botón de guardado. Guarda todos los campos (editados o no)...
        buttonSave.setOnClickListener {
            val name = editedName.text.toString()
            val lastname = editedLastname.text.toString()
            val location = editedLocation.text.toString()
            val aboutMe = editedAboutMe.text.toString()

            // Siempre que no estén vacíos (a excepción del aboutMe):
            if(name.isNotEmpty() && lastname.isNotEmpty() && location.isNotEmpty()){

                // El aboutMe no puede tener una longitud igual o mayor a 140 caracteres:
                if(aboutMe.length <= 140){

                    // Cambiamos al objeto los atributos deseados:
                    profileUser.setName(name)
                    profileUser.setLastname(lastname)
                    profileUser.setLocation(location)
                    profileUser.setAboutMe(aboutMe)

                    // Lo persistimos a la bade de datos:
                    FirebaseDatabaseModel.registerCarer(userUid,profileUser)

                    // Avisamos y cambiamos la vista actual con los nuevos datos:
                    Toast.makeText(this,"Tus datos han sido editados correctamente",Toast.LENGTH_SHORT).show()
                    profileName.text = name + " " + lastname
                    profileLocation.text = location
                    profileBio.text = aboutMe

                    profileSheetDialog.dismiss()
                }else{
                    makeToast("Por favor, escribe menos de 140 caracteres en acerca de ti")
                }

            }else{
                makeToast("Te faltan datos")
            }
        }
    }

    // Función que cambia la visibilidad de los botones de eliminar y añadir fotografías de perfil:
    private fun changeButtonsVisibility(){
        if(imagesMapIsInitialized && imagesMap.isNotEmpty()){
            buttonRemove.visibility = View.VISIBLE
        }
        if(imagesMapIsInitialized && imagesMap.size < 5){
            buttonAdd.visibility = View.VISIBLE
        }else if(!imagesMapIsInitialized){
            buttonAdd.visibility = View.VISIBLE
        }
    }

}