package com.paco.mascocuida.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import com.paco.mascocuida.adapters.PetsAdapter
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
* Esta actividad muestra las mascotas que tiene un dueño en un RecyclerView.
*/
class PetsActivity : AppCompatActivity() {

    // Declaración de los elementos de la interfaz:
    private lateinit var buttonAdd: Button
    private lateinit var textNoPets: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de los elementos de la interfaz y del auth (así como del UID actual del usuario):
        buttonAdd = findViewById(R.id.button_add)
        textNoPets = findViewById(R.id.text_nopets)

        auth = Firebase.auth
        val userUid = auth.currentUser?.uid


        // Lanzamos una corrutina para mostrar los datos del RecyclerView:
        CoroutineScope(Dispatchers.Main).launch {

            // Extraemos un HashMap de mascotas e inicializamos el adaptador con los datos si el mapa no está vacio:
            val petMap = FirebaseDatabaseModel.listPets(userUid)
            val petsAdapter = PetsAdapter(petMap)
            val recyclerView: RecyclerView = findViewById(R.id.recycler_view_pets)
            if(petMap.isNotEmpty()){
                recyclerView.adapter = petsAdapter
            }else{
                // Si el usuario aún no ha registrado ninguna mascota se verá un mensaje, se oculta el RecyclerView y
                // obviamos el adaptador:
                textNoPets.isVisible = true
                recyclerView.isVisible = false
                recyclerView.adapter = null
            }
        }

        // Listener del botón de añadir mascota. Lleva a la Actividad que maneja la lógica de adición/edición de mascotas:
        buttonAdd.setOnClickListener{
            val intent = Intent(this,ManagePetActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}