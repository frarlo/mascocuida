package com.paco.mascocuida.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
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
import com.paco.mascocuida.models.FirebaseAuthModel
import com.paco.mascocuida.models.FirebaseDatabaseModel
import com.paco.mascocuida.models.FirebaseStorageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CarerActivity : AppCompatActivity() {

    private lateinit var buttonRequests: Button
    private lateinit var buttonServices: Button
    private lateinit var buttonProfile: Button
    private lateinit var buttonTerms: Button
    private lateinit var buttonLogout: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var textUser: TextView
    private lateinit var imageWelcome: ImageView
    private lateinit var picBitmap: Bitmap
    private lateinit var picUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textUser = findViewById(R.id.text_user)
        imageWelcome = findViewById(R.id.image_welcome)
        buttonRequests = findViewById(R.id.button_requests)
        buttonServices = findViewById(R.id.button_services)
        buttonProfile = findViewById(R.id.button_profile)
        buttonTerms = findViewById(R.id.button_terms)
        buttonLogout = findViewById(R.id.button_logout)

        picUri = Uri.EMPTY

        // Inicializamos la variable de Firebase Auth:
        auth = Firebase.auth

        // Accedemos al UID del usuario y declaramos la imagen de perfil
        val userId = auth.currentUser?.uid
        var profilePicRef: String? = null

        // Lanzamos una corrutina para manejar la operación asíncrona con Firebase:
        CoroutineScope(Dispatchers.Main).launch{

            // Extraemos el objeto Owner que representa al usuario actual:
            val carerObject = FirebaseDatabaseModel.getCarerFromFirebase(userId)

            // Personalizamos la interfaz con su nombre e imagen (utilizamos Glide para esta última):
            textUser.text = carerObject?.getName()
            profilePicRef = carerObject?.getPic()

            Glide.with(this@CarerActivity).load(profilePicRef).into(imageWelcome)

        }

        // Listener del botón de solicitudes:
        buttonRequests.setOnClickListener {

            // Lanzamos una corrutina para comprobar si el usuario tiene servicios por aceptar o rechazar:
            CoroutineScope(Dispatchers.Main).launch {

                val servicesMap = FirebaseDatabaseModel.listCarerServices(userId)
                val pendingServicesMap = servicesMap.filterValues { it.status == "pending" }

                // Si los tiene inicia la Actividad para mostrarlos:
                if(pendingServicesMap.isNotEmpty()){
                    // If yes -> Activity
                    val intent = Intent(this@CarerActivity,ServicesActivity::class.java)
                    intent.putExtra("typeListing","requests")
                    intent.putExtra("userRole","Carer")
                    startActivity(intent)
                }else{
                    Toast.makeText(this@CarerActivity,"No tienes solicitudes",Toast.LENGTH_SHORT).show()
                }
                // En caso contrario muestra un toast y evita ejecutar la Activity.
            }

        }

        // El usuario quiere acceder a sus servicios:
        buttonServices.setOnClickListener {
            val intent = Intent(this,ServicesActivity::class.java)
            intent.putExtra("typeListing","others")
            intent.putExtra("userRole","Carer")
            startActivity(intent)
        }

        // Listener del botón de perfil - Lleva a la actividad del perfil (para verlo y/o editarlo):
        buttonProfile.setOnClickListener{
            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }

        // Listener del botón de los términos:
        buttonTerms.setOnClickListener {
            showTerms()
        }

        // Listener del botón de deslogueo:
        buttonLogout.setOnClickListener{
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

                if(userId != null){
                    CoroutineScope(Dispatchers.Main).launch {
                        // Ponemos la nueva imagen en el perfil del usuario:
                        val profilePicUrl = FirebaseStorageModel.createProfilePic(userId,picUri)
                        // Actualizamos su referencia en la base de datos:
                        if(profilePicUrl != null){
                            FirebaseDatabaseModel.updateProfilePic(userId,"carers",profilePicUrl)
                        }
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