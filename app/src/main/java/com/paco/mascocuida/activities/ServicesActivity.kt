package com.paco.mascocuida.activities

import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
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
    private lateinit var linearRequests: LinearLayout
    private lateinit var linearAccepted: LinearLayout
    private lateinit var textRequest: TextView
    private lateinit var textAccepted: TextView
    private lateinit var textCompleted: TextView
    private lateinit var linearCompleted: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_services)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        linearRequests = findViewById(R.id.linear_requests)
        textRequest = findViewById(R.id.text_request)
        linearAccepted = findViewById(R.id.linear_accepted)
        textAccepted = findViewById(R.id.text_accepted)
        linearCompleted = findViewById(R.id.linear_completed)
        textCompleted = findViewById(R.id.text_completed)
        /* TODO - discriminar si el usuario que accede a esta actividad es dueño o cuidador. Si es dueño se invocará
        un método de FirebaseDatabase y si es cuidador otro. Cómo hacemos esto? accediendo al rol que está guardado en SharedPreferences
        */

        // TODO - Inflar vistas

        auth = Firebase.auth
        val userUid = auth.currentUser?.uid


        // Lanzamos la corrutina para mostrar los datos del RecyclerView:


        val userRole = intent.getStringExtra("userRole").toString()

        val typeService = intent.getStringExtra("typeListing").toString()

        if(userRole == ("Carer")){

            // If typeService is "requests" only show them.
            if(typeService == "requests"){

                // Ignore accepted and completed views:
                linearAccepted.isVisible = false
                linearCompleted.isVisible = false
                // Show requests
                CoroutineScope(Dispatchers.Main).launch {
                    val servicesMap = FirebaseDatabaseModel.listCarerServices(userUid)
                    val pendingServicesMap = servicesMap.filterValues { it.status == "pending" }
                    val recyclerViewPending: RecyclerView = findViewById(R.id.recycler_pending)
                    if(pendingServicesMap.isNotEmpty()){
                        val pendingServicesAdapter = ServicesAdapter(pendingServicesMap,"Carer","pending")
                        recyclerViewPending.adapter = pendingServicesAdapter
                    }
                }

            }
            // If typeService is "listing" show accepted and completed
            else if(typeService == "others"){

                // Ignore requests:
                linearRequests.isVisible = false
                // Show accepted and completed:
                CoroutineScope(Dispatchers.Main).launch {

                    val servicesMap = FirebaseDatabaseModel.listCarerServices(userUid)
                    val acceptedServicesMap = servicesMap.filterValues { it.status == "accepted" }
                    val recyclerViewAccepted: RecyclerView = findViewById(R.id.recycler_accepted)
                    val completedServicesMap = servicesMap.filterValues {it.status == "completed"}
                    val recyclerViewCompleted: RecyclerView = findViewById(R.id.recycler_completed)

                    if(acceptedServicesMap.isNotEmpty()){
                        val acceptedServicesAdapter = ServicesAdapter(acceptedServicesMap,"Carer","accepted")
                        recyclerViewAccepted.adapter = acceptedServicesAdapter
                    }else{
                        textAccepted.isVisible = true
                    }

                    if(completedServicesMap.isNotEmpty()){
                        val completedServicesAdapter = ServicesAdapter(completedServicesMap,"Carer","completed")
                        recyclerViewCompleted.adapter = completedServicesAdapter
                    }else{
                        textCompleted.isVisible = true
                    }
                }
            }
            // Lists all:
            /*CoroutineScope(Dispatchers.Main).launch {

                val servicesMap = FirebaseDatabaseModel.listCarerServices(userUid)
                /*val servicesAdapter = ServicesAdapter(servicesMap,"Carer","pending")
                val recyclerView: RecyclerView = findViewById(R.id.recycler_pending)
                recyclerView.adapter = servicesAdapter*/

                val pendingServicesMap = servicesMap.filterValues { it.status == "pending" }
                val recyclerViewPending: RecyclerView = findViewById(R.id.recycler_pending)
                val acceptedServicesMap = servicesMap.filterValues { it.status == "accepted" }
                val recyclerViewAccepted: RecyclerView = findViewById(R.id.recycler_accepted)
                val completedServicesMap = servicesMap.filterValues {it.status == "completed"}
                val recyclerViewCompleted: RecyclerView = findViewById(R.id.recycler_completed)


                if(pendingServicesMap.isNotEmpty()){
                    val pendingServicesAdapter = ServicesAdapter(pendingServicesMap,"Carer","pending")
                    recyclerViewPending.adapter = pendingServicesAdapter
                }

                if(acceptedServicesMap.isNotEmpty()){
                    val acceptedServicesAdapter = ServicesAdapter(acceptedServicesMap,"Carer","accepted")
                    recyclerViewAccepted.adapter = acceptedServicesAdapter
                }

                if(completedServicesMap.isNotEmpty()){
                    val completedServicesAdapter = ServicesAdapter(completedServicesMap,"Carer","completed")
                    recyclerViewCompleted.adapter = completedServicesAdapter
                }

            }*/


        }else if(userRole == "Owner"){
            Toast.makeText(this,"User es un dueño!!!",Toast.LENGTH_SHORT).show()

            // TODO - Llenar las vistas con los servicios del dueño
            CoroutineScope(Dispatchers.Main).launch {
                
                val servicesMap = FirebaseDatabaseModel.listOwnerServices(userUid)

                // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/filter-values.html
                val pendingServicesMap = servicesMap.filterValues { it.status == "pending" }
                val recyclerViewPending: RecyclerView = findViewById(R.id.recycler_pending)
                val acceptedServicesMap = servicesMap.filterValues { it.status == "accepted" }
                val recyclerViewAccepted: RecyclerView = findViewById(R.id.recycler_accepted)
                val completedServicesMap = servicesMap.filterValues {it.status == "completed"}
                val recyclerViewCompleted: RecyclerView = findViewById(R.id.recycler_completed)

                if(pendingServicesMap.isNotEmpty()){
                    val pendingServicesAdapter = ServicesAdapter(pendingServicesMap,"Owner","pending")
                    recyclerViewPending.adapter = pendingServicesAdapter
                }else{
                    textRequest.isVisible = true
                    recyclerViewPending.adapter = null
                }

                if(acceptedServicesMap.isNotEmpty()){
                    val acceptedServicesAdapter = ServicesAdapter(acceptedServicesMap,"Owner","accepted")
                    recyclerViewAccepted.adapter = acceptedServicesAdapter
                }else{
                    textAccepted.isVisible = true
                    recyclerViewAccepted.adapter = null
                }

                if(completedServicesMap.isNotEmpty()){
                    val completedServicesAdapter = ServicesAdapter(completedServicesMap,"Owner","completed")
                    recyclerViewCompleted.adapter = completedServicesAdapter
                }else{
                    textCompleted.isVisible = true
                    recyclerViewCompleted.adapter = null
                }
            }
        }
    }
}