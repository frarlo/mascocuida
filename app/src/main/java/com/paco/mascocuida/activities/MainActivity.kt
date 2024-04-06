package com.paco.mascocuida.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R

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
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Log.d("MainActivity","Current user is $currentUser")
        if (currentUser != null) {
            val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)

            userRole = sharedPreferences.getString("userRole","").toString()


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

        }else{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}