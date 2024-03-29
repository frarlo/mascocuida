package com.paco.mascocuida.activities

import android.os.Bundle
import android.provider.MediaStore.Audio.Radio
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
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
import java.util.UUID

class ManagePetActivity : AppCompatActivity() {

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


        // TODO: Discernir si el botón que lanza esta aplicación (con un intent put extra podría valer)
        // quiere mostrar la vista de adición o de edición. Si es adición los campos en blanco, si es edición
        // en el intent podría ir el objeto tipo "Pet" y luego modificarlo. La visibilidad del botón "eliminar" solo estará
        // disponible en la vista de edición, obviamente.

        // TODO: Cuando se guarde, se tiene que persistir el objeto a la BD y, preferiblemente, mostrar un mensaje de guardado con un toast
        // y volver a la vista de mis mascotas.

        petName = findViewById(R.id.text_pet_name)
        petSpecies = findViewById(R.id.spinner_species)
        radioGroupSize = findViewById(R.id.radio_size)
        radioGroupAge = findViewById(R.id.radio_age)
        checkLikesDogs = findViewById(R.id.check_likes_dogs)
        checkLikesCats = findViewById(R.id.check_likes_cats)
        radioGroupGender = findViewById(R.id.radio_sex)
        checkSterilised = findViewById(R.id.check_sterilised)
        buttonSave = findViewById(R.id.button_save)


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

                // Creamos un nuevo UID para la mascota:
                val petUid = UUID.randomUUID().toString()

                // Creamos el objeto POJO:
                val newPet = Pet(userUid,petUid,petName,petSpecies,selectedSize,selectedAge,selectedGender,likesDogs,likesCats,isSterilised)

                FirebaseDatabaseModel.addPet(userUid,petUid,newPet)

                // TODO: Mensaje de confirmación y paso a otra actividad
                Toast.makeText(this,"Se ha registrado a $petName correctamente",Toast.LENGTH_SHORT).show()


            }else{
                Toast.makeText(this,"Faltan datos...",Toast.LENGTH_SHORT).show()
            }

        }


    }


    private fun compruebaCampos(userName: String, userLastname: String, userLocation: String,
                                userEmail: String, userEmailConfirmation: String,
                                userPassword: String, userPasswordConfirmation: String): Boolean{

        return userName.isNotEmpty()

    }
}