package com.paco.mascocuida.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.adapters.CarersAdapter
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * Actividad que lista todos los cuidadores que existen en nuestra aplicación. Ofrece un RecyclerView que aprovecha
 * el adaptador personalizado (CarersAdapter). Ofrecemos también dos opciones de ordenación, para poder ordenar por
 * puntuación media de cuidados y por localización del usuario.
 */
class ListCarersActivity : AppCompatActivity() {

    // Declaramos los elementos de la interfaz y dos valores booleanos para invertir los resultados:
    private lateinit var sortByRating: Button
    private lateinit var sortByLocation: Button
    private lateinit var textOrder: TextView
    private var invertedRating: Boolean = false
    private var invertedLocation: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_carers)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializamos vistas y botones:
        sortByRating = findViewById(R.id.button_sort_rating)
        sortByLocation = findViewById(R.id.button_sort_location)
        textOrder = findViewById(R.id.text_order)


        // Lanzamos la corrutina para mostrar los datos del RecyclerView:
        CoroutineScope(Dispatchers.Main).launch {
            val carersList = FirebaseDatabaseModel.listAllCarers()
            val carersAdapter = CarersAdapter(carersList)
            val recyclerView: RecyclerView = findViewById(R.id.recycler_carers_list)
            recyclerView.adapter = carersAdapter
        }

        // Listener del botón de ordenación por puntuación:
        sortByRating.setOnClickListener {
            sortBy("rating")
        }

        // Listener del botón de ordenación por localización:
        sortByLocation.setOnClickListener {
            sortBy("location")
        }
    }

    // Función que recoge el ordenamiento de los cuidadores por rating (ascendente o descendente mediante un valor booleano):
    private fun sortBy(typeSort: String){

        // Lanzamos la corrutina para mostrar los datos del RecyclerView:
        CoroutineScope(Dispatchers.Main).launch {

            // Inspirado en https://www.baeldung.com/kotlin/sort

            // Obtenemos la lista de cuidadores y la ordenamos por rating de mayor a menor:
            val carersList = FirebaseDatabaseModel.listAllCarers()
            if(typeSort == "rating"){
                // Si invertedRating es false ordenará la lista de mayor a menor y si es true viceversa:
                if(!invertedRating){
                    carersList.sortByDescending{it.getRating()}
                    this@ListCarersActivity.invertedRating = true
                    this@ListCarersActivity.textOrder.text = "Orden actual: puntuación descendiente."
                }else{
                    carersList.sortBy{it.getRating()}
                    this@ListCarersActivity.invertedRating = false
                    this@ListCarersActivity.textOrder.text = "Orden actual: puntuación ascendente."

                }
            }else if (typeSort == "location"){
                if(invertedLocation){
                    carersList.sortBy{it.getLocation()}
                    this@ListCarersActivity.invertedLocation = false
                    this@ListCarersActivity.textOrder.text = "Orden actual: localización ascendente."
                }else{
                    carersList.sortByDescending{it.getLocation()}
                    this@ListCarersActivity.invertedLocation = true
                    this@ListCarersActivity.textOrder.text = "Orden actual: localización descendente."
                }
            }
            // Finalmente asignamos el adaptador con la lista a nuestra recycler view:
            val carersAdapter = CarersAdapter(carersList)
            val recyclerView: RecyclerView = findViewById(R.id.recycler_carers_list)
            recyclerView.adapter = carersAdapter
        }
    }
}