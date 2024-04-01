package com.paco.mascocuida.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Carer
import com.paco.mascocuida.data.User
import com.paco.mascocuida.models.FirebaseAuthModel
import com.paco.mascocuida.models.FirebaseDatabaseModel
import com.paco.mascocuida.models.FirebaseStorageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class RegisterActivity : AppCompatActivity() {

    // Declaramos las variables que recogen todos los elementos de la interfaz:
    private lateinit var userCarerRole: RadioButton
    private lateinit var userOwnerRole: RadioButton
    private lateinit var userName: EditText
    private lateinit var userLastname: EditText
    private lateinit var userLocation: EditText
    private lateinit var userPic: ImageView
    private lateinit var picBitmap: Bitmap
    private lateinit var picUri: Uri
    private lateinit var buttonPic: Button
    private lateinit var userEmail: EditText
    private lateinit var userEmailConfirmation: EditText
    private lateinit var userPassword: EditText
    private lateinit var userPasswordConfirmation: EditText
    private lateinit var buttonRegister: Button

    // Declaramos la variable que controla la autentificación en Firebase:
    private lateinit var auth: FirebaseAuth
    // Lo mismo para Firebase storage:
    private lateinit var storage: FirebaseStorage
    // Base de datos:
    //private lateinit var database: FirebaseDatabase
    //private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de los edit text y los botones:
        userCarerRole = findViewById(R.id.radio_carer_role)
        userOwnerRole = findViewById(R.id.radio_owner_role)
        userName = findViewById(R.id.text_firstname)
        userLastname = findViewById(R.id.text_lastname)
        userLocation = findViewById(R.id.text_location)
        userPic = findViewById(R.id.image_profilepic)
        buttonPic = findViewById(R.id.button_pic)
        userEmail = findViewById(R.id.text_email)
        userEmailConfirmation = findViewById(R.id.text_emailconfirm)
        userPassword = findViewById(R.id.text_password)
        userPasswordConfirmation = findViewById(R.id.text_passwordconfirm)
        buttonRegister = findViewById(R.id.button_register)

        // Inicializamos el auth/storage de Firebase:
        auth = Firebase.auth
        storage = Firebase.storage

        // Asignamos una foto de perfil predeterminada que se muestra en la vista y se carga en el recurso
        userPic.setImageResource(R.drawable.predefined_userpic)
        picBitmap = BitmapFactory.decodeResource(resources, R.drawable.predefined_userpic)
        picUri = Uri.parse("android.resource://${packageName}/${R.drawable.predefined_userpic}")

        // No establecemos como obligatoria poner una foto de perfil personal, así que dejamos una predefinida

        // Usamos PhotoPicker https://developer.android.com/training/data-storage/shared/photopicker
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                // Se intentó utilizar "ImageDecoder" pero requiere un nivel de API superior. Se usa una solución
                // más antigua, obsoleta pero válida:
                // Inicializamos la variable de la imagen de perfil con la URI que selecciona el usuario:
                picBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                // Para comprimir y ahorrar datos seguimos: https://stackoverflow.com/a/43885809
                val baos = ByteArrayOutputStream()
                picBitmap.compress(Bitmap.CompressFormat.JPEG,25, baos)
                // La mostramos por la interfaz
                userPic.setImageBitmap(picBitmap)
                // Actualizamos el valor de picUri para que apunte a la imagen seleccionada:
                picUri = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        // Listener del botón de imagen:
        buttonPic.setOnClickListener{
            // Llamamos a la Activity según lo dispuesto en la documentación oficial de Android:
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Listener del botón:
        buttonRegister.setOnClickListener{
            val userName = userName.text.toString()
            val userLastname = userLastname.text.toString()
            val userLocation = userLocation.text.toString()
            val userEmail = userEmail.text.toString()
            val userEmailConfirmation = userEmailConfirmation.text.toString()
            val userPassword = userPassword.text.toString()
            val userPasswordConfirmation = userPasswordConfirmation.text.toString()

            // Comprobamos que el usuario ha rellenado todos los campos requeridos:
            if(compruebaCampos(userName, userLastname, userLocation,
                    userEmail, userEmailConfirmation, userPassword, userPasswordConfirmation)){

                // Comprobamos que ambos emails y contraseñas coinciden:
                if(userEmail == userEmailConfirmation && userPassword == userPasswordConfirmation){

                    // Creamos una nueva corrutina en la que lanzamos los procesos asíncronos de registro y perfil:
                    CoroutineScope(Dispatchers.Main).launch {

                        FirebaseAuthModel.createFirebaseUser(userEmail,userPassword)
                        // Extraemos el identificador único del usuario, que lo identifica de manera única
                        //val userId = Firebase.auth.currentUser?.uid.toString()
                        val user = FirebaseAuth.getInstance().currentUser
                        val userId = user?.uid

                        Log.d("RegisterActivity","El UID del usuario actual es $userId")

                        // El userId no es nulo, así que podemos seguir con el registro de sus datos:
                        if (!userId.isNullOrEmpty()){

                            // Inicializamos la url de la imagen en el Bucket a nula:
                            var profilePicUrl: String? = null

                            // Creamos una corrutina para manejar la subida asíncrona de la imagen de perfil:
                            CoroutineScope(Dispatchers.Main).launch{

                                profilePicUrl = FirebaseStorageModel.createProfilePic(userId,picUri)

                                if(userCarerRole.isChecked){
                                    // Instanciamos un nuevo objeto tipo usuario con los datos recogidos:
                                    //val newCarer = User(userId,"Carer",userName, userLastname, userLocation,
                                        //profilePicUrl,userEmail)

                                    val testCarer = Carer(userId,"Carer",userName, userLastname, userLocation,
                                        profilePicUrl,userEmail,0.0,"Escribe algo sobre ti.")
                                    FirebaseDatabaseModel.registerCarer(userId,testCarer)

                                    // Llamamos al método para introducirlo en la colección:
                                    //FirebaseDatabaseModel.registerNewCarer(userId,newCarer)

                                }else{
                                    val newOwner = User(userId,"Owner",userName, userLastname, userLocation,
                                        profilePicUrl,userEmail)
                                    FirebaseDatabaseModel.registerNewOwner(userId,newOwner)
                                }
                            }
                        }
                        makeToast("Usuario registrado exitosamente. Ahora puedes loguearte")
                        // Registro acabado, volvemos al login:
                        val intent = Intent(this@RegisterActivity,LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }else{
                    makeToast("El email o contraseña no coinciden")
                }
            }else{
                makeToast("Introduce los datos necesarios")
            }
        }
    }

    // Función para comprobar si están vacíos los campos introducibles:
    private fun compruebaCampos(userName: String, userLastname: String, userLocation: String,
                                userEmail: String, userEmailConfirmation: String,
                                userPassword: String, userPasswordConfirmation: String): Boolean{

        return userName.isNotEmpty() && userLastname.isNotEmpty() && userLocation.isNotEmpty() &&
                userEmail.isNotEmpty() && userEmailConfirmation.isNotEmpty() &&
                userPassword.isNotEmpty() && userPasswordConfirmation.isNotEmpty()

    }

    // Función que realiza toasts de forma modular:
    private fun makeToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

}