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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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

class OwnerActivity : AppCompatActivity() {

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

        picUri = Uri.EMPTY

        // Accedemos al UID del usuario y declaramos la imagen de perfil
        val userId = auth.currentUser?.uid
        var profilePicRef: String? = null

        // Lanzamos una corrutina para manejar la operación asíncrona con Firebase:
        CoroutineScope(Dispatchers.Main).launch{

            // Extraemos el objeto Owner que representa al usuario actual:
            val ownerObject = FirebaseDatabaseModel.getOwnerFromFirebase(userId)

            // Personalizamos la interfaz con su nombre e imagen (utilizamos Glide para esta última):
            textUser.text = ownerObject?.getName()
            profilePicRef = ownerObject?.getPic()

            Glide.with(this@OwnerActivity).load(profilePicRef).into(imageWelcome)

        }


        // Listener. El usuario quiere gestionar sus mascotas.
        buttonPets.setOnClickListener {
            val intent = Intent(this,PetsActivity::class.java)
            startActivity(intent)
        }

        // Listener. El usuario quiere acceder a sus cuidados.
        buttonServices.setOnClickListener {
            val intent = Intent(this, ServicesActivity::class.java)
            intent.putExtra("userRole","Owner")
            startActivity(intent)
        }

        // Listener. El usuario quiere ver un listado de los cuidadores.
        buttonCarers.setOnClickListener {
            val intent = Intent(this, ListCarersActivity::class.java)
            startActivity(intent)
        }

        // Listener. El usuario quiere modificar sus datos personales.
        buttonProfile.setOnClickListener {
            // TODO - Launch intent (or pop up) to personalize user's data
        }

        // Listener del botón de los términos:
        buttonTerms.setOnClickListener {
            showTerms()
        }

        // Listener el usuario quiere desloguearse de la aplicación:
        buttonLogout.setOnClickListener {
            // TODO - Confirm with pop-up. If yes execute the logout.

            // Inicializamos una instancia de SharedPreferences para guardar los datos de logueo:
            val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply(){
                putString("userRole","empty")
                apply()
            }

            FirebaseAuthModel.logoutFirebaseUser()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
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
                            FirebaseDatabaseModel.updateProfilePic(userId,"owners",profilePicUrl)
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
}