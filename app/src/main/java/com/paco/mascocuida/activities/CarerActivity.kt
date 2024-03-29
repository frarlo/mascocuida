package com.paco.mascocuida.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.paco.mascocuida.R
import com.paco.mascocuida.models.FirebaseAuthModel

class CarerActivity : AppCompatActivity() {

    private lateinit var buttonProfile: Button


    private lateinit var buttonLogout: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonProfile = findViewById(R.id.button_profile)


        buttonLogout = findViewById(R.id.button_logout)


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

        }

    }
}