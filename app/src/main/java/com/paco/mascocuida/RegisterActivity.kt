package com.paco.mascocuida

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.paco.mascocuida.data.User


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
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseRef: DatabaseReference

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
        // Declaramos e inicializamos la referencia de storage:
        val storageRef = storage.reference

        // Inicializamos la base de datos:
        database = FirebaseDatabase.getInstance("https://mascocuida-a-default-rtdb.europe-west1.firebasedatabase.app")
        databaseRef = Firebase.database.reference


        // Usamos PhotoPicker https://developer.android.com/training/data-storage/shared/photopicker
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                // Se intentó utilizar "ImageDecoder" pero requiere un nivel de API superior. Se usa una solución
                // más antigua, obsoleta pero válida:
                // Inicializamos la variable de la imagen de perfil con la URI que selecciona el usuario:
                picBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
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

            // TODO: 1. Comprobar primero que todos los campos están completos - Falta comprobar imagen TODO !
            if(compruebaCampos(userName, userLastname, userLocation,
                    userEmail, userEmailConfirmation, userPassword, userPasswordConfirmation)){

                // TODO: 1.5. Comprobar que ambos emails coinciden y, por separado, que ambas contraseñas también:
                if(userEmail == userEmailConfirmation && userPassword == userPasswordConfirmation){
                    // TODO: 2. Registrar al usuario en Firebase Auth - Mover a función modular
                    // Firebase auth, según lo expuesto en la documentación oficial de Firebase:
                    createFirebaseUser(userEmail, userPassword)
                    // Extraemos el identificador único del usuario, que lo identifica de manera única

                    val userId = Firebase.auth.currentUser?.uid

                    // TODO: Función para la subida a Firebase Storage separada eg: uploadProfilePic(userId)

                    if (userId != null){
                        // TODO: 2.5. Subir la foto de perfil a Firebase Storage - Su enlace estará guardado en la colección
                        // Declaramos el File a subir:
                        Log.d("Valor URI","VALOR URI $picUri")
                        val file = picUri
                        Log.d("Ejecución dentro del bloque Firebase Storage","Picuri: $picUri")
                        // Ponemos la referencia al Bucket que queremos:
                        val profilePicsRef = storageRef.child("profile_pics/profile$userId")
                        Log.d("Ejecución dentro del bloque Firebase Storage","Picuri: $profilePicsRef")

                        // Subimos la imagen
                        val uploadTask = profilePicsRef.putFile(file)

                        var profilePicUrl: String? = null

                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener {
                            Log.d("Ejecución dentro del bloque Firebase Storage HA FALLADO","ERROR en $uploadTask")

                        }.addOnSuccessListener { taskSnapshot ->
                            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                                // Guardamos el enlace a la imagen de perfil en una constante:
                                profilePicUrl = uri.toString()
                                // Si el usuario ha marcado su rol como cuidador...
                                if(userCarerRole.isChecked){
                                    Log.d("Ejecución entra a bucle de userCarerRole.isChecked","Por ahora bien")
                                    // Crea un documento en la colección de cuidadores:
                                    val newCarer = User(userId,"Carer",userName, userLastname, userLocation,
                                        profilePicUrl,userEmail)

                                    // Lo introducimos en la colección:
                                    registerNewCarer(userId,newCarer)

                                    // Si la ejecución llega aquí es que es el otro rol el que está seleccionado:
                                }else{
                                    // Crea un documento en la colección de dueños:
                                    val newOwner = User(userId,"Owner",userName, userLastname, userLocation,
                                        profilePicUrl,userEmail)

                                    registerNewOwner(userId,newOwner)
                                }
                            }

                        }



                    }

                }else{
                    Toast.makeText(this,"El email o contraseña no coincide",Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(this,"Introduce los datos necesarios.",Toast.LENGTH_SHORT).show()
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

    // Función que crea y registra al usuario en Firebase AUTH: - Devuelve al Usuario instanciado:
    private fun createFirebaseUser(userEmail: String, userPassword: String) {

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                    // Actualizar UI SEGÚN NOTAS UpdateUI(user)
                    // TODO: Inicializar aquí intent del Main Activity
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }

            }
    }

    // Función para actualizar la interfaz con el Usuario - Documentación de Firebase
    private fun updateUI(user: FirebaseUser?) {
    }

    // Registra al usuario con su id de Firebase auth y la información del constructor:
    private fun registerNewOwner(userId: String, newOwner: User){

        databaseRef.child("owners").child(userId).setValue(newOwner)
    }

    private fun registerNewCarer(userId: String, newCarer: User){
        Log.d("registerNewCarer","Ejecución en método de inserción")
        databaseRef.child("carers").child(userId).setValue(newCarer)

    }
}