package com.paco.mascocuida.models

import android.provider.ContactsContract.Data
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.data.Pet
import com.paco.mascocuida.data.User
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDatabaseModel {
    companion object {

        // Base de datos y referencia:
        private val database = FirebaseDatabase.getInstance("https://mascocuida-a-default-rtdb.europe-west1.firebasedatabase.app")
        private var databaseRef = database.reference

        // Función que registra un nuevo dueño:
        fun registerNewOwner(userId: String, newOwner: User) {
            databaseRef.child("owners").child(userId).setValue(newOwner)

        }

        // Función que registra un nuevo cuidador:
        fun registerNewCarer(userId: String, newCarer: User) {
            databaseRef.child("carers").child(userId).setValue(newCarer)
        }

        suspend fun listAllCarers(): MutableList<User>{
            return suspendCoroutine {continuation ->
                val carersList = mutableListOf<User>()
                val carersRef = databaseRef.child("carers")

                carersRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (carerSnapshot in snapshot.children){
                            val carer = carerSnapshot.getValue<User>()
                            if (carer != null){
                                carersList.add(carer)
                            }
                        }
                        continuation.resume(carersList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }

        // Función que añade (y edita) una mascota de un dueño:
        fun addPet(userId: String, petUid: String, pet: Pet) {
            databaseRef.child("owners").child(userId).child("pets").child(petUid).setValue(pet)
        }

        // Función que quita una mascota al dueño:
        fun removePet(userId: String, petUid: String){
            databaseRef.child("owners").child(userId).child("pets").child(petUid).removeValue()
        }

        // Función que lista todas las mascotas de un dueño y devuelve una lista de objetos Pet en forma de MutableList:
        suspend fun listPets(userId: String?): MutableList<Pet> {
            return suspendCoroutine {continuation ->

                val petList = mutableListOf<Pet>()

                if(userId != null){
                    val ownerPetsRef = databaseRef.child("owners").child(userId).child("pets")

                    ownerPetsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot){

                            for (petSnapshot in snapshot.children){
                                val pet = petSnapshot.getValue<Pet>()
                                if(pet != null){
                                    petList.add(pet)
                                }
                            }

                            continuation.resume(petList)
                        }

                        override fun onCancelled(error: DatabaseError){

                        }
                    })
                }
            }
        }

        // Función que lista una mascota específica:
        suspend fun listSinglePet(userId: String?, petUid: String?): Pet? {
            return suspendCoroutine { continuation ->
                if(userId != null && petUid != null){

                    val petRef = databaseRef.child("owners").child(userId).child("pets").child(petUid)

                    petRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val pet = snapshot.getValue<Pet>()
                                continuation.resume(pet)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }
            }
        }


        suspend fun getUserFromFirebase(userId: String?): User? {
            return suspendCoroutine {continuation ->
                if(userId != null) {
                    val ownersRef = databaseRef.child("owners").child(userId)
                    val carersRef = databaseRef.child("carers").child(userId)

                    ownersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Log.d("FB","Usuario es owner y ha sido encontrado, inicializando objeto")
                                val user = snapshot.getValue<User>()
                                Log.d("FB","Objeto $user")
                                continuation.resume(user)
                            } else {
                                carersRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val user = snapshot.getValue<User>()
                                            Log.d("FB","Objeto $user")
                                            continuation.resume(user)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.d("DatamodelFirebase","Error $error")
                                    }
                                })
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("DatamodelFirebase","Error $error")
                        }
                    })
                }
            }
        }

    }
}