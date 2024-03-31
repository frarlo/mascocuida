package com.paco.mascocuida.activities

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore.Audio.Radio
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Pet
import com.paco.mascocuida.models.FirebaseDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ManagePetActivity : AppCompatActivity() {

    private lateinit var textLanding: TextView
    private lateinit var petName: EditText
    private lateinit var petSpecies: Spinner
    private lateinit var radioGroupSize: RadioGroup
    private lateinit var radioGroupAge: RadioGroup
    private lateinit var checkLikesDogs: CheckBox
    private lateinit var checkLikesCats: CheckBox
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var checkSterilised: CheckBox
    private lateinit var buttonSave: Button
    private lateinit var buttonDelete: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_pet)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val user = Firebase.auth.currentUser
        val userUid = user?.uid.toString()

        textLanding = findViewById(R.id.text_managing_landing)
        petName = findViewById(R.id.text_pet_name)
        petSpecies = findViewById(R.id.spinner_species)
        radioGroupSize = findViewById(R.id.radio_size)
        radioGroupAge = findViewById(R.id.radio_age)
        checkLikesDogs = findViewById(R.id.check_likes_dogs)
        checkLikesCats = findViewById(R.id.check_likes_cats)
        radioGroupGender = findViewById(R.id.radio_sex)
        checkSterilised = findViewById(R.id.check_sterilised)
        buttonSave = findViewById(R.id.button_save)
        buttonDelete = findViewById(R.id.button_delete)

        // Extraemos el UID de la mascota del extra del intent:
        val existentPetUid = intent.getStringExtra("petUid")

        // Si la cadena está vacía o está nula no hace nada (la vista será de creación) pero si la cadena contiene algo
        // como el UID de la mascota se inicializará la vista en modo de "edición", con todos los datos de la mascota
        // preproblados y con un botón adicional.
        if(existentPetUid.isNullOrEmpty()){
            textLanding.text = "Registra una nueva mascota" // TODO - Translate
        }else{
            textLanding.text = "Edita tu mascota"
            CoroutineScope(Dispatchers.Main).launch {
                val editedPet = FirebaseDatabaseModel.listSinglePet(userUid,existentPetUid)

                if (editedPet != null){

                    petName.setText(editedPet.getName())

                    val petObjectSpecies = editedPet.getSpecies()
                    if(petObjectSpecies.equals("Perro")){
                        petSpecies.setSelection(0)
                    }else if (petObjectSpecies.equals("Gato")){
                        petSpecies.setSelection(1)
                    }

                    val petObjectSize = editedPet.getSize()
                    if (petObjectSize.equals("Pequeño")){
                        radioGroupSize.check(R.id.radio_size_small)
                    }else if (petObjectSize.equals("Mediano")){
                        radioGroupSize.check(R.id.radio_size_medium)
                    }else if (petObjectSize.equals("Grande")){
                        radioGroupSize.check(R.id.radio_size_large)
                    }

                    val petObjectAge = editedPet.getAge()
                    if (petObjectAge.equals("Cachorro")){
                        radioGroupAge.check(R.id.radio_age_junior)
                    }else if (petObjectAge.equals("Adulto")){
                        radioGroupAge.check(R.id.radio_age_adult)
                    }else if (petObjectAge.equals("Senior")){
                        radioGroupAge.check(R.id.radio_age_senior)
                    }

                    // Likes dogs
                    if(editedPet.getLikesDogs() == true){
                        checkLikesDogs.isChecked = true
                    }

                    // Likes cats
                    if(editedPet.getLikesCats() == true){
                        checkLikesCats.isChecked = true
                    }

                    // Gender
                    val petObjectGender = editedPet.getGender()
                    if(petObjectGender.equals("Macho")){
                        radioGroupGender.check(R.id.radio_sex_male)
                    }else{
                        radioGroupGender.check(R.id.radio_sex_female)
                    }

                    // Sterilised
                    if(editedPet.getIsSterilised() == true){
                        checkSterilised.isChecked = true
                    }

                    // Button delete visibility
                    buttonDelete.visibility = View.VISIBLE

                }

            }

        }



        buttonSave.setOnClickListener {

            val petName = petName.text.toString().trim()
            val petSpecies = petSpecies.selectedItem.toString()
            var selectedSize: String? = null
            var selectedAge: String? = null
            var selectedGender: String? = null

            // BASED ON: https://mkyong.com/android/android-radio-buttons-example/
            val intSize = radioGroupSize.checkedRadioButtonId

            if(intSize == R.id.radio_size_small){
                selectedSize = "Pequeño"
            }else if(intSize == R.id.radio_size_medium){
                selectedSize = "Mediano"
            }else if(intSize == R.id.radio_size_large){
                selectedSize = "Grande"
            }

            val intAge = radioGroupAge.checkedRadioButtonId

            if(intAge == R.id.radio_age_junior){
                selectedAge = "Cachorro"
            }else if(intAge == R.id.radio_age_adult){
                selectedAge = "Adulto"
            }else if(intAge == R.id.radio_age_senior){
                selectedAge = "Senior"
            }

            val likesDogs = checkLikesDogs.isChecked
            val likesCats = checkLikesCats.isChecked

            val intGender = radioGroupGender.checkedRadioButtonId
            if(intGender == R.id.radio_sex_male){
                selectedGender = "Macho"
            }else if(intGender == R.id.radio_sex_female){
                selectedGender = "Hembra"
            }

            val isSterilised = checkSterilised.isChecked

            // Comprobamos si la mascota tiene un nombre:
            if(petName.isNotEmpty()){

                if(existentPetUid.isNullOrEmpty()) {

                    // Creamos un nuevo UID para la mascota:
                    val petUid = UUID.randomUUID().toString()

                    // Creamos el objeto POJO:
                    val newPet = Pet(userUid, petUid, petName, petSpecies, selectedSize,
                        selectedAge, selectedGender, likesDogs, likesCats, isSterilised)

                    FirebaseDatabaseModel.addPet(userUid, petUid, newPet)

                    Toast.makeText(this, "Se ha registrado a $petName correctamente", Toast.LENGTH_SHORT).show()

                    launchPetsActivity()

                }else{

                    // Creamos un nuevo objeto POJO con los campos editados:
                    val editedPet = Pet(userUid, existentPetUid, petName, petSpecies, selectedSize,
                        selectedAge, selectedGender, likesDogs, likesCats, isSterilised)

                    FirebaseDatabaseModel.addPet(userUid, existentPetUid, editedPet)

                    // TODO: Mensaje de confirmación y paso a otra actividad
                    Toast.makeText(this, "Se han modificado los datos de $petName correctamente", Toast.LENGTH_SHORT).show()

                    launchPetsActivity()
                }

            }else{
                Toast.makeText(this,"Faltan datos...",Toast.LENGTH_SHORT).show()
            }

        }

        buttonDelete.setOnClickListener {
            // TODO: WARNING MESSAGE WITH POP UP?
            if (existentPetUid != null){

                // TODO: Comprobar si el animal está en algún servicio actual --

                FirebaseDatabaseModel.removePet(userUid,existentPetUid)


                // TODO: Volver a la vista de mascotas:


                launchPetsActivity()

            }
        }

    }


    private fun launchPetsActivity(){
        val intent = Intent(this, PetsActivity::class.java)
        startActivity(intent)
        finish()
    }

}