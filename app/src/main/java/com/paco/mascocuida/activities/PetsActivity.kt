package com.paco.mascocuida.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

class PetsActivity : AppCompatActivity() {

    private lateinit var buttonAdd: Button
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

        buttonAdd = findViewById(R.id.button_add)
        auth = Firebase.auth
        val userUid = auth.currentUser?.uid


        // Lanzamos la corrutina para mostrar los datos del RecyclerView:
        CoroutineScope(Dispatchers.Main).launch {

            val petMap = FirebaseDatabaseModel.listPets(userUid)
            val petsAdapter = PetsAdapter(petMap)
            val recyclerView: RecyclerView = findViewById(R.id.recycler_view_pets)
            recyclerView.adapter = petsAdapter

        }

        buttonAdd.setOnClickListener{
            val intent = Intent(this,ManagePetActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}