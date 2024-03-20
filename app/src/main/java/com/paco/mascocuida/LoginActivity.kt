package com.paco.mascocuida

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    // Declaramos los input y los botones:
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button
    private lateinit var buttonForgot: Button

    // Declaramos la variable que controla la autentificación en Firebase:
    private lateinit var auth: FirebaseAuth

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

        // Inicializamos el auth de Firebase:
        auth = Firebase.auth

        // Listener del botón de Registro:
        buttonRegister.setOnClickListener{
            // TODO: Debe llevar a la Actividad "RegisterActivity"
            val intent = Intent(this, RegisterActivity::class.java)
            // Start
            startActivity(intent)
        }

        // Listener del botón de Olvido:
        buttonForgot.setOnClickListener{
            // TODO: Debe llevar a la Actividad o Fragment! "ForgotCredentials"
        }

        // Listener del botón de Login:
        buttonLogin.setOnClickListener {
            // TODO:
            //  1. Debe comprobar los datos en Firebase
            //  2. Debe mostrar, en caso de introducir MAL los datos que ha fallado la autentificación
            val userEmail = userEmail.text.toString()
            val userPassword = userPassword.text.toString()

            // Comprobamos si el usuario ha introducido algo en los dos campos:
            if (compruebaCampos(userEmail, userPassword)) {

                // Logueamos al usuario con sus credenciales siguiendo las instrucciones de Firebase
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Se loguea
                            val user = auth.currentUser
                            updateUI(user)
                            Log.d("LoginActivity", "Logueo exitoso con $user")

                        } else {
                            // Datos erróneos:
                            Toast.makeText(this, "Datos incorrectos.", Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                        Log.d("LoginActivity", "Dirección de correo electrónico: $userEmail")

                    }
            }else{
                Toast.makeText(this, "Introduce tus datos", Toast.LENGTH_SHORT).show()
            }
        }

    }


    // Función para comprobar si están vacíos los campos introducibles:
    private fun compruebaCampos(userEmail: String, userPassword: String): Boolean{
        // Devuelve un booleano basado en el valor contrario de ambos campos:
        return !TextUtils.isEmpty(userEmail) || !TextUtils.isEmpty(userPassword)
    }

    // Función para actualizar la interfaz con el Usuario - Documentación de Firebase
    private fun updateUI(user: FirebaseUser?) {
    }
}