package com.paco.mascocuida.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth // If ktx.auth causes issues
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
* Esta clase es un modelo que abstrae y permite acceder desde cualquier lugar de la aplicación a la lógica
* de autorización de Firebase Authentication. Podemos registrar y desloguear usuarios, así como envíar correos
* electrónicos de reinicio de contraseñas.
*/
class FirebaseAuthModel {
    companion object {

        // Variables que inicializan tanto la instancia de autentificación de Firebase como el usuario:
        private var auth: FirebaseAuth = Firebase.auth
        private var user: FirebaseUser? = null

        // Función que registra a un usuario en Firebase y devuelve un objeto que lo representa:
        suspend fun createFirebaseUser(userEmail: String, userPassword: String): FirebaseUser? =
            suspendCoroutine { continuation ->
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user = auth.currentUser
                            continuation.resume(user)
                        }
                    }
            }

        // Función que envía un email para resetear la contraseña en caso de no recordarla:
        fun forgottenFirebaseCredentials(userEmail: String) {
            auth.sendPasswordResetEmail(userEmail)
        }

        // Función que desloguea a un usuario de Firebase:
        fun logoutFirebaseUser() {
            auth.signOut()
        }

    }
}
