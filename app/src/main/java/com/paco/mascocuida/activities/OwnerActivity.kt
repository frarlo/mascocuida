package com.paco.mascocuida.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.paco.mascocuida.R

class OwnerActivity : AppCompatActivity() {

    private lateinit var buttonPets: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_owner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonPets = findViewById(R.id.button_pets)

        buttonPets.setOnClickListener {
            val intent = Intent(this,PetsActivity::class.java)
            startActivity(intent)
        }

        // TODO: Implement rest of buttons
    }
}