package com.paco.mascocuida.models

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
*  Esta clase es un modelo que abstrae y permite acceder desde cualquier lugar de la aplicación al almacenamiento de
*  Firebase. Es decir, aquí guardamos, actualizamos y borramos todos los recursos de la aplicación que se guardan
*  en la nube. En este caso son imágenes de perfil (atómicas para cada usuario de la aplicación) y colecciones de imágenes
*  de perfil para los cuidadores.
*/
class FirebaseStorageModel {
    companion object {

        private var storage: FirebaseStorage = Firebase.storage
        private var storageRef = storage.reference

        // Función que guarda (o actualiza) la imagen de perfil del usuario:
        suspend fun createProfilePic(userId: String, file: Uri): String? =
            suspendCoroutine { continuation ->

                var profilePicUrl: String? = null

                val profilePicsRef = storageRef.child("user_pics/$userId/profile_pic/profile$userId")

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

        // Función que guarda una imagen que tenga el Cuidador en su perfil personal público:
        suspend fun uploadCarerPic(userId: String, file: Uri): String? =
            suspendCoroutine { continuation ->

                var carerPicUrl: String? = null

                val picUid = UUID.randomUUID().toString()

                val profilePicsRef = storageRef.child("user_pics/$userId/carer_pic/$picUid")

                val uploadTask = profilePicsRef.putFile(file)

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener {

                }.addOnSuccessListener { taskSnapshot ->
                    /* Siguiendo la documentación de Firebase añadimos listeners. En caso de éxito, y sabiendo
                    que "taskSnapshot" contiene los metadatos del archivo subido... */

                    // Creamos otra subtarea para que accedamos a la URI de descarga de la imagen:
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        // Actualizamos el valor de la variable con la uri pasada a cadena:
                        carerPicUrl = uri.toString()
                        continuation.resume(carerPicUrl)
                    }
                }
            }

        // Función que borra una imagen que tenga el Cuidador en su perfil personal público:
        fun removeCarerPic(picUrl: String){
            val fileRef = storage.getReferenceFromUrl(picUrl)
            fileRef.delete()
        }

    }
}