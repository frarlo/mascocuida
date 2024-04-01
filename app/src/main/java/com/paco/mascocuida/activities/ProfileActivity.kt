package com.paco.mascocuida.activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.paco.mascocuida.models.FirebaseDatabaseModel
import com.paco.mascocuida.models.FirebaseStorageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePics: ViewPager
    private lateinit var buttonAdd: FloatingActionButton
    private lateinit var profileName: TextView
    private lateinit var profileStars: TextView
    private lateinit var profileBio: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAction: Button
    private lateinit var user: FirebaseUser
    private lateinit var userUid: String
    private lateinit var picUri: Uri

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
            // Se podrá ver el botón de añadir fotos al perfil:
            buttonAdd.visibility = View.VISIBLE
            // TODO - Declarar todo lo editable como tal:
            // Función que se encarga de la edición:
            profileIsEditable()
        // Si existe es que es un dueño "cotilleando"
        }else{
            // TODO - Declarar, si hace falta, algún campo como "read only"

            profileIsReadOnly()
        }
    }

    private fun profileIsReadOnly(){

        buttonAction.text = "Solicitar cuidado"
    }

    private fun profileIsEditable(){

        CoroutineScope(Dispatchers.Main).launch {
            val editableUser = FirebaseDatabaseModel.getCarerFromFirebase(userUid)

            if(editableUser != null){

                if(!editableUser.getPics().isNullOrEmpty()){

                    CoroutineScope(Dispatchers.Main).launch {
                        val imagesMap = FirebaseDatabaseModel.listCarerPics(userUid)
                        val imagesAdapter = ImageAdapter(imagesMap)
                        profilePics.adapter = imagesAdapter
                        Toast.makeText(
                            this@ProfileActivity,
                            "Se han encontrado imagenes",
                            Toast.LENGTH_SHORT
                        ).show()
                        for((key,value) in imagesMap){
                            Log.d("ImagesMapContent","Key: $key / Url: $value")
                        }
                    }



                }

                val userFullName = editableUser.getName() + " " + editableUser.getLastname()
                profileName.text = userFullName

                val userRating = editableUser.getRating()
                val formattedRating = String.format("%.2f", userRating)
                if(userRating == null){
                    profileStars.text = "0/5"
                }else{
                    profileStars.text = formattedRating + "/5"
                }


                buttonAction.text = "Guardar cambios"
            }


        }



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
}