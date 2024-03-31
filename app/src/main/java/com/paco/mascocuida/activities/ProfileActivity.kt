package com.paco.mascocuida.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePics: ViewPager
    private lateinit var profileName: TextView
    private lateinit var profileStars: TextView
    private lateinit var profileBio: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAction: Button
    private lateinit var user: FirebaseUser
    private lateinit var userUid: String

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

        profilePics = findViewById(R.id.viewpager_profile)
        profileName = findViewById(R.id.textview_username)
        profileStars = findViewById(R.id.textview_stars)
        profileBio = findViewById(R.id.text_bio)
        recyclerView = findViewById(R.id.recycler_reviews)
        buttonAction = findViewById(R.id.button_action)


        // TODO - Discriminar si la actividad viene desde un dueño que quiere dejar a la mascota o es el prio
        // cuidador que quiere ver/editar su perfil.

        // ACCEDER AL INTENT PUT EXTRA (SI esta actividad la inicia el propio usuario cuidador no habrá intent extra pero
        // si el perfil lo está viendo el owner habrá intent extra con el UID del cuidador seleccionado. Compararemos el UID pasado
        // con el UID del usuario actual. Si existe la variable será para ver el perfil de forma "READ" y si no existe es el
        // propio cuidador el que está accediendo a su perfil, por lo que podrá editar.

        // Extraemos el UID del cuidador del extra del intent:
        val existentCarerUid = intent.getStringExtra("carerUid")

        // Si no existe es que es el propio usuario el que está accediendo a su perfil:
        if(existentCarerUid.isNullOrEmpty()){
            profileIsEditable()
        // Si existe es que es un dueño "cotilleando"
        }else{

            profileIsReadOnly()
        }
    }

    private fun profileIsReadOnly(){

        buttonAction.setText("Solicitar cuidado")
    }

    private fun profileIsEditable(){
        CoroutineScope(Dispatchers.Main).launch {
            val editableUser = FirebaseDatabaseModel.getCarerFromFirebase(userUid)

            if(editableUser != null){
            }

            val userFullName = editableUser?.getName() + " " + editableUser?.getLastname()
            profileName.text = userFullName

            val userRating = editableUser?.getRating()
            val formattedRating = String.format("%.2f", userRating)
            if(userRating == null){
                profileStars.text = "0/5"
            }else{

                profileStars.text = formattedRating + "/5"
            }


            buttonAction.setText("Guardar cambios")

        }
    }
}