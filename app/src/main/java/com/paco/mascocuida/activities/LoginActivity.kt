package com.paco.mascocuida.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.paco.mascocuida.R
import com.paco.mascocuida.models.FirebaseAuthModel
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Declaramos los input y los botones:
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button
    private lateinit var buttonForgot: Button

    // Declaramos la variable que controla la autentificación en Firebase:
    private lateinit var auth: FirebaseAuth

    // Declaramos las variables del pop up emergente:
    private lateinit var builder: AlertDialog.Builder
    private lateinit var view: View
    private lateinit var buttonReset: Button
    private lateinit var forgottenEmail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de los edit text y los botones:
        userEmail = findViewById(R.id.user_email)
        userPassword = findViewById(R.id.user_password)
        buttonLogin = findViewById(R.id.button_login)
        buttonRegister = findViewById(R.id.button_register)
        buttonForgot = findViewById(R.id.button_forgot)

        // Inicializamos la interfaz de autentificación de Firebase:
        auth = Firebase.auth

        // Listener del botón de Registro:
        buttonRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            // Start
            startActivity(intent)
        }

        // Listener del botón de Olvido:
        buttonForgot.setOnClickListener{
            forgottenCredentials()
        }

        // Listener del botón de Login:
        buttonLogin.setOnClickListener {
            // TODO:
            //  1. Debe comprobar los datos en Firebase
            //  2. Debe mostrar, en caso de introducir MAL los datos que ha fallado la autentificación
            val userEmail = userEmail.text.toString()
            val userPassword = userPassword.text.toString()

            // Comprobamos si el usuario ha introducido algo en los dos campos:
            if (checkFields(userEmail, userPassword)) {
                Log.d("LoginActivity","Usuario ha puesto email y contraseña")
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this) { task ->
                        Log.d("LoginActivity","Usuario se ha logueado")
                        if (task.isSuccessful) {
                            // Se loguea
                            val user = auth.currentUser
                            updateUI(user)
                            //TODO: Inicio Actividad en función del rol:

                            var userRole: String? = null
                            val userUid = user?.uid.toString()

                            CoroutineScope(Dispatchers.Main).launch {
                                Log.d("LoginActivity","Lanzando corrutina para sacar el objeto con $userUid")
                                val objectUser = FirebaseDatabaseModel.getUserFromFirebase(userUid)
                                Log.d("LoginActivity","objectUser is $objectUser")
                                if (objectUser != null) {
                                    userRole = objectUser.getUserRole()
                                    Log.d("LoginActivity","userRole has returned $userRole")
                                }

                                // CRUD: Tenemos que averiguar el ROL del usuario antes de ejecutar una u otra actividad:
                                // El userId es el nombre del documento que puede estar en una colección o en otra.

                                if(userRole.equals("Owner")) {
                                    makeToast("DUEÑO. Variable: $userRole")
                                    val intent = Intent(this@LoginActivity, OwnerActivity::class.java)
                                    startActivity(intent)
                                }else if(userRole.equals("Carer")){
                                    makeToast("CUIDADOR. Variable: $userRole")
                                    val intent = Intent(this@LoginActivity, CarerActivity::class.java)

                                    startActivity(intent)
                                }else {
                                    makeToast("Ejecución no supo obtener el rol: $userRole")
                                }





                            }


                        }else{
                            makeToast("Credenciales incorrectas")
                        }
                    }
            }else{
                    makeToast("Completa los campos requeridos")
            }
        }
    }


    // Función para comprobar si están vacíos los campos introducibles:
    private fun checkFields(userEmail: String, userPassword: String): Boolean{
        return userEmail.isNotEmpty() || userPassword.isNotEmpty()
    }

    // Función para actualizar la interfaz con el Usuario - Documentación de Firebase
    private fun updateUI(user: FirebaseUser?) {
        // TODO
    }

    // Función que instancia e infla un pop-up que se encarga de reiniciar la contraseña del usuario:
    private fun forgottenCredentials(){
        builder = AlertDialog.Builder(this@LoginActivity).setCancelable(true)
        view = layoutInflater.inflate(R.layout.forgotten_credentials, null)
        buttonReset = view.findViewById(R.id.button_reset)
        forgottenEmail = view.findViewById(R.id.text_emailreset)
        builder.setView(view)

        var email: String

        val popUp = builder.create()

        popUp.show()

        buttonReset.setOnClickListener{

            email = forgottenEmail.text.toString().trim()

            if(email.isNotEmpty()){
                // Comprobamos que sea un email válido con el método:
                if(compruebaEmail(email)){

                    FirebaseAuthModel.forgottenFirebaseCredentials(email)
                    makeToast("Comprueba tu correo electrónico")


                }else{
                    makeToast("Introduce un correo válido")
                }
            }else{
                makeToast("Introduce un email")
            }
        }
    }

    // Método para comprobar el email:
    //https://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
    private fun compruebaEmail(email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Sencilla función para hacer Toasts de forma modular:
    private fun makeToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}