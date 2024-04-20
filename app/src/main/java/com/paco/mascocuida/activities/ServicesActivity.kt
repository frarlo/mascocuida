package com.paco.mascocuida.activities

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
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

/* Esta actividad maneja la visualización de los servicios en sus distintos estatus. Discriminamos entonces según rol y
* estatus del servicio gracias a los parámetros que reciba la Actividad en el putExtra.
*/
class ServicesActivity : AppCompatActivity() {

    // Declaración del auth y de los elementos de las vistas (layouts y textViews):
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

        // Inicializamos vistas:
        linearRequests = findViewById(R.id.linear_requests)
        textRequest = findViewById(R.id.text_request)
        linearAccepted = findViewById(R.id.linear_accepted)
        textAccepted = findViewById(R.id.text_accepted)
        linearCompleted = findViewById(R.id.linear_completed)
        textCompleted = findViewById(R.id.text_completed)

        // Accedemos al usuario actual que está viendo esta actividad:
        auth = Firebase.auth
        val userUid = auth.currentUser?.uid

        // Extraemos el rol del usuario y el tipo de servicio que queremos listar:
        val userRole = intent.getStringExtra("userRole").toString()
        val typeService = intent.getStringExtra("typeListing").toString()

        // Si el usuario es un cuidador:
        if(userRole == ("Carer")){

            // Y el "typeService" es exclusivamente "requests", mostraremos solamente el layout de solicitudes:
            if(typeService == "requests"){

                // Ocultamos las vistas aceptadas y completadas:
                linearAccepted.isVisible = false
                linearCompleted.isVisible = false

                // Lanzamos una corrutina para extraer las solicitudes en un HashMap:
                CoroutineScope(Dispatchers.Main).launch {

                    // Obtenemos el HashMap:
                    val servicesMap = FirebaseDatabaseModel.listCarerServices(userUid)

                    // Filtramos los servicios con una expresión Lambda según lo expuesto en
                    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/filter-values.html
                    val pendingServicesMap = servicesMap.filterValues { it.status == "pending" }

                    // Inicializamos la vista del recycler view y si el mapa no esta vacio pasamos el HashMap filtrado
                    // al adaptador:
                    val recyclerViewPending: RecyclerView = findViewById(R.id.recycler_pending)
                    if(pendingServicesMap.isNotEmpty()){
                        val pendingServicesAdapter = ServicesAdapter(pendingServicesMap,"Carer","pending")
                        recyclerViewPending.adapter = pendingServicesAdapter
                    }
                }
            }
            // Si el "typeService" es "otros". Mostraremos aceptados y completados:
            else if(typeService == "others"){

                // Ocultamos el Layout que contiene el RecyclerView de solicitudes:
                linearRequests.isVisible = false

                // Lanzamos una corrutina para extraer el listado de servicios:
                CoroutineScope(Dispatchers.Main).launch {

                    // Obtenemos el HashMap con todos los servicios de cuidador:
                    val servicesMap = FirebaseDatabaseModel.listCarerServices(userUid)

                    // Filtramos el servicesMap con dos expresiones lambda y luego inicializamos las RecyclerView:
                    val acceptedServicesMap = servicesMap.filterValues { it.status == "accepted" }
                    val recyclerViewAccepted: RecyclerView = findViewById(R.id.recycler_accepted)
                    val completedServicesMap = servicesMap.filterValues {it.status == "completed"}
                    val recyclerViewCompleted: RecyclerView = findViewById(R.id.recycler_completed)

                    // Si el HashMap de servicios aceptados no esta vacío se puebla el RecyclerView con sus valores:
                    if(acceptedServicesMap.isNotEmpty()){
                        val acceptedServicesAdapter = ServicesAdapter(acceptedServicesMap,"Carer","accepted")
                        recyclerViewAccepted.adapter = acceptedServicesAdapter
                    }else{
                        // En caso contrario mostramos un texto informativo al usuario:
                        textAccepted.isVisible = true
                    }

                    // Lo mismo de arriba pero para servicios completados:
                    if(completedServicesMap.isNotEmpty()){
                        val completedServicesAdapter = ServicesAdapter(completedServicesMap,"Carer","completed")
                        recyclerViewCompleted.adapter = completedServicesAdapter
                    }else{
                        textCompleted.isVisible = true
                    }
                }
            }

        // Si el usuario es un dueño los tres tipos principales de estatus se muestran con sus layouts:
        }else if(userRole == "Owner"){

            // Lanzamos una corrutina, como hemos visto antes, para extraer el HashMap de todos los servicios
            // y luego filtrar con expresiones Lambda y mostrar dinámicamente los RecyclerView:
            CoroutineScope(Dispatchers.Main).launch {
                
                val servicesMap = FirebaseDatabaseModel.listOwnerServices(userUid)

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