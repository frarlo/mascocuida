package com.paco.mascocuida.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Owner
import com.paco.mascocuida.models.FirebaseAuthModel
import com.paco.mascocuida.models.FirebaseDatabaseModel
import com.paco.mascocuida.models.FirebaseStorageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/*
* Esta actividad es la pantalla de bienvenida de los usuarios con el rol Dueño. Permite acceder a todas las funciones
* de nuestra aplicación:
*/
class OwnerActivity : AppCompatActivity() {

    // Declaración de los elementos de la interfaz:
    private lateinit var buttonPets: Button
    private lateinit var buttonServices: Button
    private lateinit var buttonCarers: Button
    private lateinit var buttonProfile: Button
    private lateinit var buttonTerms: Button
    private lateinit var buttonLogout: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var textUser: TextView
    private lateinit var imageWelcome: ImageView
    private lateinit var picBitmap: Bitmap
    private lateinit var picUri: Uri
    private lateinit var ownerObject: Owner
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_owner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializamos todos los elementos de la interfaz:
        textUser = findViewById(R.id.text_user)
        imageWelcome = findViewById(R.id.image_welcome)
        buttonPets = findViewById(R.id.button_pets)
        buttonServices = findViewById(R.id.button_services)
        buttonCarers = findViewById(R.id.button_carers)
        buttonProfile = findViewById(R.id.button_profile)
        buttonTerms = findViewById(R.id.button_terms)
        buttonLogout = findViewById(R.id.button_logout)

        // Inicializamos la variable de Firebase Auth:
        auth = Firebase.auth

        // Para evitar excepciones inicializamos la variable de tipo uri a vacía:
        picUri = Uri.EMPTY

        // Accedemos al UID del usuario y declaramos la imagen de perfil
        userId = auth.currentUser?.uid.toString()
        var profilePicRef: String? = null

        // Lanzamos una corrutina para manejar la operación asíncrona con Firebase:
        CoroutineScope(Dispatchers.Main).launch{

            // Extraemos el objeto Owner que representa al usuario actual:
            ownerObject = FirebaseDatabaseModel.getOwnerFromFirebase(userId)!!

            // Personalizamos la interfaz con su nombre e imagen (utilizamos Glide para esta última):
            textUser.text = ownerObject.getName()
            profilePicRef = ownerObject.getPic()
            Glide.with(this@OwnerActivity).load(profilePicRef).into(imageWelcome)
        }

        // Listener. El usuario quiere gestionar sus mascotas y lleva a otra actividad:
        buttonPets.setOnClickListener {
            val intent = Intent(this,PetsActivity::class.java)
            startActivity(intent)
        }

        // Listener. El usuario quiere acceder a sus cuidados y lleva a otra actividad:
        buttonServices.setOnClickListener {
            val intent = Intent(this, ServicesActivity::class.java)
            intent.putExtra("userRole","Owner")
            startActivity(intent)
        }

        // Listener. El usuario quiere ver un listado de los cuidadores y lleva a otra actividad:
        buttonCarers.setOnClickListener {
            val intent = Intent(this, ListCarersActivity::class.java)
            startActivity(intent)
        }

        // Listener. El usuario quiere modificar sus datos personales y llama a otro método:
        buttonProfile.setOnClickListener {
            editInformation()
        }

        // Listener del botón de los términos (inicializa una función):
        buttonTerms.setOnClickListener {
            showTerms()
        }

        // Listener del botón de deslogueo (inicializa una función):
        buttonLogout.setOnClickListener {
            popUpLogout()
        }

        // Usamos PhotoPicker como en el Registro:
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                picBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val baos = ByteArrayOutputStream()
                picBitmap.compress(Bitmap.CompressFormat.JPEG,25, baos)
                imageWelcome.setImageBitmap(picBitmap)
                picUri = uri

                // Lanzamos una corrutina para actualizar la imagen de perfil y su referencia en la base de datos:
                CoroutineScope(Dispatchers.Main).launch {

                    val profilePicUrl = FirebaseStorageModel.createProfilePic(userId,picUri)

                    if(profilePicUrl != null){
                        FirebaseDatabaseModel.updateProfilePic(userId,"owners",profilePicUrl)
                        ownerObject.setPic(profilePicUrl)
                        Toast.makeText(this@OwnerActivity,"Imagen de perfil actualizada",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Usuario pulsa sobre su imagen de perfil, abrimos una instancia para editar su imagen:
        imageWelcome.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    // Función que infla un BottomSheet con los TCs:
    private fun showTerms(){
        val termsSheetDialog = BottomSheetDialog(this)
        val viewSheet = LayoutInflater.from(this).inflate(R.layout.terms_sheet,null)
        termsSheetDialog.setContentView(viewSheet)
        termsSheetDialog.show()
    }

    // Función que muestra un pop-up de tipo SheetDialog para editar la información personal de forma simple:
    private fun editInformation(){

        // Inflamos la vista y la mostramos:
        val profileSheetDialog = BottomSheetDialog(this)
        val viewSheet = LayoutInflater.from(this).inflate(R.layout.info_sheet,null)
        profileSheetDialog.setContentView(viewSheet)
        profileSheetDialog.show()

        // Inicializamos las variables de la vista:
        val editedName: EditText = viewSheet.findViewById(R.id.text_firstname)
        val editedLastname: EditText = viewSheet.findViewById(R.id.text_lastname)
        val editedLocation: EditText = viewSheet.findViewById(R.id.text_location)
        val buttonSave: Button = viewSheet.findViewById(R.id.button_save)

        // Poblamos la vista:
        editedName.setText(ownerObject.getName())
        editedLastname.setText(ownerObject.getLastname())
        editedLocation.setText(ownerObject.getLocation())

        // Listener del botón de guardado:
        buttonSave.setOnClickListener {

            // Obtenemos los datos de lo introducido:
            val name = editedName.text.toString()
            val lastname = editedLastname.text.toString()
            val location = editedLocation.text.toString()

            // Si ningún campo esta vacío...
            if(name.isNotEmpty() && lastname.isNotEmpty() && location.isNotEmpty()){
                // Cambiamos al objeto tipo dueño los atributos con set:
                ownerObject.setName(name)
                ownerObject.setLastname(lastname)
                ownerObject.setLocation(location)

                // Lo persistimos a la base de datos y mostramos un mensaje informativo:
                FirebaseDatabaseModel.registerOwner(userId,ownerObject)
                Toast.makeText(this,"Tus datos han sido editados correctamente",Toast.LENGTH_SHORT).show()

                // Tenemos que actualizar la interfaz con el nuevo nombre:
                textUser.text = name

                // Cerramos el diálogo:
                profileSheetDialog.dismiss()

            // El usuario ha dejado algo sin completar:
            }else{
                Toast.makeText(this,"Te faltan datos",Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función que recoge la intención de desloguearse de la aplicación y muestra un pop-up de confirmación:
    private fun popUpLogout(){

        // Inflado del pop-up de la advertencia de deslogueo:
        val builder = AlertDialog.Builder(this).setCancelable(true)
        val view = layoutInflater.inflate(R.layout.reusable_popup, null)
        val buttonNo = view.findViewById<Button>(R.id.button_pop_left)
        val buttonYes = view.findViewById<Button>(R.id.button_pop_right)
        val textPop = view.findViewById<TextView>(R.id.pop_up_header)
        val subTextPop = view.findViewById<TextView>(R.id.pop_up_subheader)
        buttonNo.text = "Volver"
        buttonYes.text = "Salir"
        textPop.text = "Cerrar sesión"
        subTextPop.text = "Volverás a la pantalla de inicio"

        // Lo construimos y lo mostramos:
        builder.setView(view)
        val popUp = builder.create()
        popUp.show()

        // Si dice no el pop-up se cierra y no pasa nada:
        buttonNo.setOnClickListener {
            popUp.dismiss()
        }

        // En caso contrario, el usuario confirma que quiere salir:
        buttonYes.setOnClickListener {

            // Inicializamos una instancia de SharedPreferences para actualizar los datos de logueo:
            val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply{
                putString("userRole","empty")
                apply()
            }

            // Lo deslogueamos y lanzamos la actividad de Login:
            FirebaseAuthModel.logoutFirebaseUser()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}