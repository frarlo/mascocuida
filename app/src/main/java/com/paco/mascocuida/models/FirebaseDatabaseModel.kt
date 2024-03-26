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
import com.paco.mascocuida.data.User
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDatabaseModel {
    companion object {

        // Base de datos y referencia:
        private val database = FirebaseDatabase.getInstance("https://mascocuida-a-default-rtdb.europe-west1.firebasedatabase.app")
        private var databaseRef = database.reference

        fun registerNewOwner(userId: String, newOwner: User) {
            databaseRef.child("owners").child(userId).setValue(newOwner)

        }

        fun registerNewCarer(userId: String, newCarer: User) {
            databaseRef.child("carers").child(userId).setValue(newCarer)
        }

        fun registerNewUser(userId: String, newUser: User){
            databaseRef.child("users").child(userId).setValue(newUser)
        }

        suspend fun getUserFromDatabase(userId: String?): User?{

            return suspendCoroutine { continuation ->
                if (userId != null){
                    val usersRef = databaseRef.child("users").child(userId)
                    usersRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            if(snapshot.exists()){
                                val user = snapshot.getValue<User>()
                                continuation.resume(user)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }else{
                    Log.d("FIREBASEDATABASEMODEL","User Id not found in realtime database, what")
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

        // Función para comprobar el rol del usuario
        suspend fun checkRole(userId: String?): String? =
            suspendCoroutine { continuation ->
                val ownersRef = databaseRef.child("owners").child(userId ?: "")
                val carersRef = databaseRef.child("carers").child(userId ?: "")

                ownersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userRole = snapshot.child("userRole").getValue(String::class.java)
                            continuation.resume(userRole)
                        } else {
                            carersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val userRole =
                                            snapshot.child("userRole").getValue(String::class.java)
                                        continuation.resume(userRole)
                                    } else {
                                        continuation.resume(null) // El usuario no existe en ninguna colección
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    continuation.resume(null) // Manejar el error según sea necesario
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null) // Manejar el error según sea necesario
                    }
                })
            }



        /*suspend fun checkRole(userId: String?): String? =

            suspendCoroutine { continuation ->
                val ownersRef = databaseRef.child("owners").child(userId ?: "")
                val carersRef = databaseRef.child("carers").child(userId ?: "")

                ownersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userRole = snapshot.child("userRole").getValue(String::class.java)
                            continuation.resume(userRole)
                        } else {
                            carersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val userRole =
                                            snapshot.child("userRole").getValue(String::class.java)
                                        continuation.resume(userRole)
                                    } else {
                                        continuation.resume(null) // El usuario no existe en ninguna colección
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    continuation.resume(null) // Manejar el error según sea necesario
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null) // Manejar el error según sea necesario
                    }
                })
            }*/

    }
}