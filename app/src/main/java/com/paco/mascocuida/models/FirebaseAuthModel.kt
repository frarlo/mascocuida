package com.paco.mascocuida.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth // If ktx.auth causes issues
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Modelo para gestionar los
class FirebaseAuthModel {
    companion object {

        private var auth: FirebaseAuth = Firebase.auth
        private var user: FirebaseUser? = null

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

        suspend fun loginFirebaseUser(userEmail: String, userPassword: String): FirebaseUser? =

            suspendCoroutine { continuation ->
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user = auth.currentUser
                            continuation.resume(user)
                        }
                    }
            }

        fun forgottenFirebaseCredentials(userEmail: String) {

            auth.sendPasswordResetEmail(userEmail)

        }

        fun logoutFirebaseUser() {

            auth.signOut()
        }

        fun getCurrentUser(): FirebaseUser? {
            return user
        }

    }
}
