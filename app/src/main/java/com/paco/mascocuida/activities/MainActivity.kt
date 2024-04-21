package com.paco.mascocuida.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R

/*
*  Esta Actividad, la MainActivity, es la primera que se ejecuta en nuestra aplicación. Muestra una Splash Screen
*  mientras comprueba si ya hay un usuario logueado o no. Si lo hay lo redirige, según el rol almacenado en
*  SharedProperties, a su actividad de destino. Si no hay ningún usuario logueado redigirá a la Actividad de Login.
*
* */
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // SetFlags y Looper (onStart) siguiendo el ejemplo de https://www.geeksforgeeks.org/how-to-create-a-splash-screen-in-android-using-kotlin/
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        auth = Firebase.auth

    }

    public override fun onStart() {
        super.onStart()
        // Comprobamos si el usuario está logueado (no es nulo).
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Accedemos a sharedPreferences y extraemos el Rol guardado:
            val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
            userRole = sharedPreferences.getString("userRole","").toString()

            // Dependiendo del rol va a su actividad predeterminada:
            if (userRole == "Carer"){
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, CarerActivity::class.java)
                    startActivity(intent)
                    finish()
                },1000)
            }
            if (userRole == "Owner"){
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, OwnerActivity::class.java)
                    startActivity(intent)
                    finish()
                },1000)
            }
            // Para ambos incluimos un delay para que podamos ver la splashscreen. Esto es meramente estético
            // y de cara a hacer visible en la aplicación que estamos en otra Actividad. En un entorno productivo
            // esto no tiene justificación aquí.

        // Si no hay usuario logueado hay que llevar al usuario a la actividad de logueo:
        }else{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}