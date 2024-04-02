package com.paco.mascocuida.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R
import com.paco.mascocuida.models.FirebaseAuthModel
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarerActivity : AppCompatActivity() {

    private lateinit var buttonRequests: Button
    private lateinit var buttonServices: Button
    private lateinit var buttonProfile: Button
    private lateinit var buttonLogout: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var textUser: TextView
    private lateinit var imageWelcome: ImageView


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
        buttonLogout = findViewById(R.id.button_logout)

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

        buttonServices.setOnClickListener {
            val intent = Intent(this,ServicesActivity::class.java)
            intent.putExtra("userRole","Carer")
            startActivity(intent)
        }


        buttonProfile.setOnClickListener{

            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener{

            // Inicializamos una instancia de SharedPreferences para guardar los datos de logueo:
            val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply(){
                putString("userRole","empty")
                apply()
            }

            FirebaseAuthModel.logoutFirebaseUser()

            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}