package com.paco.mascocuida.models

import android.provider.ContactsContract.Data
import android.util.Log
import com.bumptech.glide.disklrucache.DiskLruCache.Value
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.paco.mascocuida.data.Carer
import com.paco.mascocuida.data.Owner
import com.paco.mascocuida.data.Pet
import com.paco.mascocuida.data.Review
import com.paco.mascocuida.data.Service
import com.paco.mascocuida.data.User
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDatabaseModel {
    companion object {

        // Base de datos (instancia) y referencia (abstracción de Firebase):
        private val database = FirebaseDatabase.getInstance("https://mascocuida-a-default-rtdb.europe-west1.firebasedatabase.app")
        private var databaseRef = database.reference

        // Función que registra un nuevo dueño:
        fun registerNewOwner(userId: String, newOwner: User) {
            databaseRef.child("owners").child(userId).setValue(newOwner)

        }

        // Función que registra a un dueño (o lo edita):
        fun registerOwner(userId: String, owner: Owner){
            databaseRef.child("owners").child(userId).setValue(owner)
        }

        // Función que registra a un cuidador (o lo edita):
        fun registerCarer(userId:String, carer: Carer){
            databaseRef.child("carers").child(userId).setValue(carer)
        }

        // Función que lista todos los cuidadores que existen en nuestra aplicación:
        suspend fun listAllCarers(): MutableList<Carer>{
            return suspendCoroutine {continuation ->
                val carersList = mutableListOf<Carer>()
                val carersRef = databaseRef.child("carers")

                carersRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (carerSnapshot in snapshot.children){
                            val carer = carerSnapshot.getValue<Carer>()
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

        // Función que añade la referencia de una imagen de perfil pública al cuidador:
        fun addCarerPic(userId: String, picUrl: String){
            databaseRef.child("carers").child(userId).child("pics").push().setValue(picUrl)
        }

        // Función que actualiza la foto de perfil del usuario (primero borra si hay alguna y luego guarda la nueva):
        fun updateProfilePic(userId: String, collection: String, picUrl: String){
            databaseRef.child(collection).child(userId).child("pic").removeValue()
            databaseRef.child(collection).child(userId).child("pic").setValue(picUrl)
        }

        // Función que borra la referencia de una imagen de perfil pública al cuidador:
        fun removeCarerPic(userId: String, picId: String){
            databaseRef.child("carers").child(userId).child("pics").child(picId).removeValue()
        }

        // Función que añade un servicio de cuidado:
        fun addService(serviceId: String, service: Service){
            databaseRef.child("services").child(serviceId).setValue(service)
        }

        // Función que borra un servicio de cuidado:
        fun removeService(serviceId: String){
            databaseRef.child("services").child(serviceId).removeValue()
        }

        // Función que lista todos los servicios que tiene/ha tenido un cuidador:
        suspend fun listCarerServices(carerId: String?): HashMap<String, Service>{
            return suspendCoroutine { continuation ->
                val carerServices = HashMap<String, Service>()
                if(carerId!= null){
                    val carerServicesRef = databaseRef.child("services").orderByChild("carerUid").equalTo(carerId)

                    carerServicesRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (serviceSnapshot in snapshot.children){
                                val serviceId = serviceSnapshot.key
                                val serviceObj = serviceSnapshot.getValue<Service>()
                                if(serviceId != null && serviceObj != null){
                                    carerServices[serviceId] = serviceObj
                                }
                            }

                            continuation.resume(carerServices)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","Error: $error")
                        }
                    })
                }
            }
        }

        suspend fun checkCarerReview(carerId: String?, serviceId: String): Boolean {
            return suspendCoroutine { continuation ->
                if(carerId != null){
                    val carerReviewsRef = databaseRef.child("carers").child(carerId)
                        .child("reviews").child(serviceId)

                    carerReviewsRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                continuation.resume(true)
                            }else if (!snapshot.exists()){
                                continuation.resume(false)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }

                }

        }
        fun createCarerReview(carerId: String, serviceId: String, review: Review){
            databaseRef.child("carers").child(carerId).child("reviews").child(serviceId).setValue(review)
        }

        // Función para listar todas las reviews de un cuidador:
        suspend fun listCarerReviews(carerId: String): HashMap<String, Review>{
            return suspendCoroutine { continuation ->
                val carerReviews = HashMap<String, Review>()
                if(carerId != null){
                    val carerReviewsRef = databaseRef.child("carers").child(carerId).child("reviews")

                    carerReviewsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (reviewSnapshot in snapshot.children){
                                val reviewId = reviewSnapshot.key
                                val reviewObj = reviewSnapshot.getValue<Review>()
                                if(reviewId != null && reviewObj != null){
                                    carerReviews[reviewId] = reviewObj
                                }
                            }
                            continuation.resume(carerReviews)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","Error while listing carer reviews: $error")
                        }

                    })
                }
            }
        }

        // Función que lista todos los servicios que ha solicitado un dueño:
        suspend fun listOwnerServices(carerId: String?): HashMap<String, Service>{
            return suspendCoroutine { continuation ->
                val ownerServices = HashMap<String, Service>()
                if(carerId!= null){
                    val carerServicesRef = databaseRef.child("services").orderByChild("ownerUid").equalTo(carerId)

                    carerServicesRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (serviceSnapshot in snapshot.children){
                                val serviceId = serviceSnapshot.key
                                val serviceObj = serviceSnapshot.getValue<Service>()
                                if(serviceId != null && serviceObj != null){
                                    ownerServices[serviceId] = serviceObj
                                }
                            }

                            continuation.resume(ownerServices)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","Error while listing Owner services: $error")
                        }
                    })
                }
            }
        }

        // Función que actualiza el estatus de un servicio:
        fun updateServiceStatus(serviceId: String, status: String){
            databaseRef.child("services").child(serviceId).child("status").setValue(status)
        }

        // Función que lista todas las referencias (enlaces) a las imágenes públicas de perfil de los cuidadores:
        suspend fun listCarerPics(userId: String?): HashMap<String,String>{
            return suspendCoroutine { continuation ->
                val carerPics = HashMap<String, String>()

                if (userId != null){
                    val carerPicsRef = databaseRef.child("carers").child(userId).child("pics")

                    carerPicsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (picSnapshot in snapshot.children){
                                val picId = picSnapshot.key
                                val picUrl = picSnapshot.getValue<String>()
                                if (picId != null && picUrl != null){
                                    carerPics[picId] = picUrl
                                }
                            }

                            continuation.resume(carerPics)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","Error while listing Carer pics: $error")
                        }
                    })
                }

            }
        }

        // Función que lista todas las mascotas de un dueño y devuelve un HashMap:
        // Al principio este método devolvia una MutableList<Pet> pero descubrimos que esto era erróneo al trabajar con Firebase:
        // https://stackoverflow.com/questions/70096815/expected-a-list-while-deserializing-but-got-a-class-java-util-hashmap-with-nest
        suspend fun listPets(userId: String?): HashMap<String, Pet> {
            return suspendCoroutine {continuation ->
                val petMap = HashMap<String, Pet>()
                if(userId != null){
                    val ownerPetsRef = databaseRef.child("owners").child(userId).child("pets")

                    ownerPetsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot){
                            for (petSnapshot in snapshot.children){
                                val petId = petSnapshot.key
                                val pet = petSnapshot.getValue<Pet>()
                                if(petId != null && pet != null){
                                    petMap[petId] = pet
                                }
                            }
                            continuation.resume(petMap)
                        }
                        override fun onCancelled(error: DatabaseError){
                            Log.d("FirebaseDatabaseModel.kt","Error while listing Owner pets: $error")
                        }
                    })
                }
            }
        }

        // Función que actualiza el rating de un cuidador cuando es llamada (cuando se realiza una nueva review):
        fun updateCarerRatings(userId: String?){
            if (userId != null){
                val reviewsRef = databaseRef.child("carers").child(userId).child("reviews")

                // Primero obtenemos la puntuación total y el total de reviews que tiene el usuario:
                reviewsRef.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var totalRating = 0.0
                        var totalReviews = 0
                        for (reviewSnapshot in snapshot.children){
                            val review = reviewSnapshot.getValue<Review>()
                            val rating = review?.getRating()
                            if(rating != null){
                                totalRating += rating
                                totalReviews ++
                            }
                        }
                        // Para luego calcular la media...
                        val averageRating = totalRating / totalReviews
                        // Y actualizar su campo en el documento del cuidador:
                        databaseRef.child("carers").child(userId).child("rating").setValue(averageRating)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("FirebaseDatabaseModel.kt","Error while updating the Carer rating: $error")
                    }
                })
            }
        }

        // Función que lista la mascota específica de un dueño:
        suspend fun listSinglePet(userId: String?, petUid: String?): Pet? {
            return suspendCoroutine { continuation ->
                if(userId != null && petUid != null){
                    val petRef = databaseRef.child("owners").child(userId).
                    child("pets").child(petUid)

                    petRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val pet = snapshot.getValue<Pet>()
                                continuation.resume(pet)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","Error while listing the Owner pet: $error")
                        }
                    })
                }
            }
        }

        // Función que extrae un objeto que representa a un cuidador en la base de datos:
        suspend fun getCarerFromFirebase(userId: String?): Carer? {
            return suspendCoroutine { continuation ->
                if (userId != null){
                    val carerRef = databaseRef.child("carers").child(userId)
                    carerRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val carer = snapshot.getValue<Carer>()
                                continuation.resume(carer)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","Error while extracting the Carer object: $error")
                        }
                    })
                }
            }
        }

        // Función que extrae un objeto que representa a un dueño de la base de datos:
        suspend fun getOwnerFromFirebase(userId: String?): Owner? {
            return suspendCoroutine { continuation ->
                if (userId != null){
                    val ownerRef = databaseRef.child("owners").child(userId)
                    ownerRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val owner = snapshot.getValue<Owner>()
                                continuation.resume(owner)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","Error while extracting the Owner object: $error")
                        }
                    })
                }
            }
        }

        // Función que devuelve un objeto que representa a un usuario o un dueño:
        suspend fun getUserFromFirebase(userId: String?): Any? {
            return suspendCoroutine {continuation ->
                    if(userId != null) {
                        val ownersRef = databaseRef.child("owners").child(userId)
                        val carersRef = databaseRef.child("carers").child(userId)

                        ownersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    Log.d("FB","Usuario es owner y ha sido encontrado, inicializando objeto")
                                    val user = snapshot.getValue<Owner>()
                                    Log.d("FB","Objeto $user")
                                    continuation.resume(user)
                                } else {
                                    carersRef.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                val user = snapshot.getValue<Carer>()
                                                Log.d("FB","Objeto $user")
                                                continuation.resume(user)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.d("FirebaseDatabaseModel.kt","Error while extracting the Carer: $error")
                                        }
                                    })
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("FirebaseDatabaseModel.kt","Error while extracting the Owner: $error")

                            }
                        })
                    }
                }
        }

    }
}