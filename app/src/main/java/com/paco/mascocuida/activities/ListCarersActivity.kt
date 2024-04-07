package com.paco.mascocuida.activities

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.adapters.CarersAdapter
import com.paco.mascocuida.adapters.PetsAdapter
import com.paco.mascocuida.data.Carer
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListCarersActivity : AppCompatActivity() {

    private lateinit var sortByRating : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_carers)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sortByRating = findViewById(R.id.button_sort_rating)


        // Lanzamos la corrutina para mostrar los datos del RecyclerView:
        CoroutineScope(Dispatchers.Main).launch {
            val carersList = FirebaseDatabaseModel.listAllCarers()
            val carersAdapter = CarersAdapter(carersList!!)
            val recyclerView: RecyclerView = findViewById(R.id.recycler_carers_list)
            recyclerView.adapter = carersAdapter
        }

        sortByRating.setOnClickListener {
            sortByRating()
        }
    }

    private fun sortByRating(){
        // Lanzamos la corrutina para mostrar los datos del RecyclerView:
        CoroutineScope(Dispatchers.Main).launch {


            val carersList = FirebaseDatabaseModel.listAllCarers()

            carersList.sortByDescending{it.getRating()}

            val carersAdapter = CarersAdapter(carersList)
            val recyclerView: RecyclerView = findViewById(R.id.recycler_carers_list)
            recyclerView.adapter = carersAdapter



        }

    }
}