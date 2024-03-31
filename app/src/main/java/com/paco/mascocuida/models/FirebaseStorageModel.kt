package com.paco.mascocuida.models

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseStorageModel {
    companion object {

        private var storage: FirebaseStorage = Firebase.storage
        private var storageRef = storage.reference

        // Función suspendida:
        suspend fun createProfilePic(userId: String, file: Uri): String? =
            suspendCoroutine { continuation ->

                var profilePicUrl: String? = null

                val profilePicsRef = storageRef.child("profile_pics/profile$userId")

                val uploadTask = profilePicsRef.putFile(file)

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener {

                }.addOnSuccessListener { taskSnapshot ->
                    /* Siguiendo la documentación de Firebase añadimos listeners. En caso de éxito, y sabiendo
                que "taskSnapshot" contiene los metadatos del archivo subido... */

                    // Creamos otra subtarea para que accedamos a la URI de descarga de la imagen:
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->

                        // Actualizamos el valor de la variable con la uri pasada a cadena:
                        profilePicUrl = uri.toString()
                        continuation.resume(profilePicUrl)
                    }

                }
            }

        // Actualiza la imagen de perfil
        fun updateProfilePic(userId: String, file: Uri) {

            // TODO: Actualizar bucket

            // TODO: Actualizar BD para que la nueva imagen esté en el documento del usuario
        }

        // Borra la imagen de perfil si el usuario quiere borrar su cuenta:
        fun deleteProfilePic(userId: String) {

            // TODO: Borrar archivo del bucket.
        }

        // TODO: Funciones para gestionar la creación, modificación y borrado de archivos de las
        // imágenes que aparecen en el perfil de los cuidadores

        suspend fun getProfilePic(userId: String, url: String){

        }
    }
}