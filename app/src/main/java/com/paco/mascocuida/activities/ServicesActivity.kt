package com.paco.mascocuida.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.paco.mascocuida.R
import com.paco.mascocuida.adapters.ServicesAdapter
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServicesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_services)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /* TODO - discriminar si el usuario que accede a esta actividad es dueño o cuidador. Si es dueño se invocará
        un método de FirebaseDatabase y si es cuidador otro. Cómo hacemos esto? accediendo al rol que está guardado en SharedPreferences
        */

        // TODO - Inflar vistas

        auth = Firebase.auth
        val userUid = auth.currentUser?.uid


        // Lanzamos la corrutina para mostrar los datos del RecyclerView:


        val userRole = intent.getStringExtra("userRole").toString()

        if(userRole == ("Carer")){

            // TODO - Llenar las vistas con los servicios del cuidador

            // Lists all:
            CoroutineScope(Dispatchers.Main).launch {

                val servicesMap = FirebaseDatabaseModel.listCarerServices(userUid)
                val servicesAdapter = ServicesAdapter(servicesMap)
                val recyclerView: RecyclerView = findViewById(R.id.recycler_pending)
                recyclerView.adapter = servicesAdapter

            }

            // Status: approved

        }else if(userRole == "Owner"){
            Toast.makeText(this,"User es un dueño!!!",Toast.LENGTH_SHORT).show()

            // TODO - Llenar las vistas con los servicios del dueño
            CoroutineScope(Dispatchers.Main).launch {
                
                val servicesMap = FirebaseDatabaseModel.listOwnerServices(userUid)
                val servicesAdapter = ServicesAdapter(servicesMap)
                val recyclerView: RecyclerView = findViewById(R.id.recycler_pending)
                recyclerView.adapter = servicesAdapter

            }

        }
    }
}