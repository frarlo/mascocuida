package com.paco.mascocuida.models

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.paco.mascocuida.data.Carer
import com.paco.mascocuida.data.Owner
import com.paco.mascocuida.data.Pet
import com.paco.mascocuida.data.Review
import com.paco.mascocuida.data.Service
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/*
* Esta clase es un modelo que abstrae y permite acceder desde cualquier lugar de la aplicación a la base de datos
* de Firebase, en este caso "Realtime Database". Aquí operamos con las típicas operaciones CRUD, en las cuales añadimos,
* actualizamos, consultamos y borramos lo relacionado con los usuarios de nuestra aplicación, sus mascotas y sus servicios.
* Por la naturaleza de la aplicación hay operaciones síncronas y asíncronas (suspend).
*/
class FirebaseDatabaseModel {
    companion object {

        // Base de datos (instancia) y referencia (abstracción de Firebase):
        private val database = FirebaseDatabase.getInstance("https://mascocuida-a-default-rtdb.europe-west1.firebasedatabase.app")
        private var databaseRef = database.reference

        // Función que registra a un dueño (o lo edita):
        fun registerOwner(userId: String, owner: Owner){
            databaseRef.child("owners").child(userId).setValue(owner)
        }

        // Función que registra a un cuidador (o lo edita):
        fun registerCarer(userId:String, carer: Carer){
            databaseRef.child("carers").child(userId).setValue(carer)
        }

        // Función asíncrona que lista todos los cuidadores que existen en nuestra aplicación:
        suspend fun listAllCarers(): MutableList<Carer>{
            return suspendCoroutine {continuation ->

                // Inicializamos una mutable list que de tipo Cuidador vacía y declaramos la referencia de los cuidadores:
                val carersList = mutableListOf<Carer>()
                val carersRef = databaseRef.child("carers")

                // Para esta referencia, vamos guardando todos los valores que se encuentren en ese documento si no son nulos:
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
                        Log.d("FirebaseDatabaseModel.kt","listAllCarers() Error: $error")
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

        // Función asíncrona que lista todos los servicios que tiene/ha tenido un cuidador:
        suspend fun listCarerServices(carerId: String?): HashMap<String, Service>{
            return suspendCoroutine { continuation ->
                val carerServices = HashMap<String, Service>()
                if(carerId!= null){
                    // La referencia de la BD está en el documento "services", donde solo sacamos los valores que pertenezcan al cuidador:
                    val carerServicesRef = databaseRef.child("services").orderByChild("carerUid").equalTo(carerId)

                    // Guardamos todos los servicios del cuidador en un HashMap:
                    carerServicesRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (serviceSnapshot in snapshot.children){
                                val serviceId = serviceSnapshot.key
                                val serviceObj = serviceSnapshot.getValue<Service>()
                                if(serviceId != null && serviceObj != null){
                                    carerServices[serviceId] = serviceObj
                                }
                            }
                            // Ya hemos extraído todos los valores, la ejecución puede continuar:
                            continuation.resume(carerServices)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","listCarerServices() Error: $error")
                        }
                    })
                }
            }
        }

        // Función asíncrona que comprueba si se ha realizado una opinión sobre un servicio:
        suspend fun checkCarerReview(carerId: String?, serviceId: String): Boolean {
            return suspendCoroutine { continuation ->
                if(carerId != null){

                    // La referencia de la BD se encuentra en la colección de cuidadores, específicamente
                    // en el documento cuyo ID coincide con el pasado y accediendo al campo de "reviews" y luego al
                    // identificador del servicio está...
                    val carerReviewsRef = databaseRef.child("carers").child(carerId)
                        .child("reviews").child(serviceId)

                    // Si existe, es que ya hay una review. Si no, no:
                    carerReviewsRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                continuation.resume(true)
                            }else if (!snapshot.exists()){
                                continuation.resume(false)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","checkCarerReview() Error: $error")
                        }
                    })
                }
            }
        }

        // Función que crea una nueva review (opinión) sobre un servicio de un cuidador:
        fun createCarerReview(carerId: String, serviceId: String, review: Review){
            databaseRef.child("carers").child(carerId).child("reviews").child(serviceId).setValue(review)
        }

        // Función para listar todas las reviews de un cuidador:
        suspend fun listCarerReviews(carerId: String): HashMap<String, Review>{
            return suspendCoroutine { continuation ->
                // Inicializamos un HashMap vacío de Reviews y obtenemos la referencia del documento del cuidador dentro
                // de la colección de cuidadores que apunta a sus reviews:
                val carerReviews = HashMap<String, Review>()
                val carerReviewsRef = databaseRef.child("carers").child(carerId).child("reviews")

                // Recorremos la referencia en busca de información.
                carerReviewsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Guardamos cada review en el HashMap si ambos no son nulos:
                        for (reviewSnapshot in snapshot.children){
                            val reviewId = reviewSnapshot.key
                            val reviewObj = reviewSnapshot.getValue<Review>()
                            if(reviewId != null && reviewObj != null){
                                carerReviews[reviewId] = reviewObj
                            }
                        }
                        // Una vez finalizado el bucle la ejecución continua:
                        continuation.resume(carerReviews)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("FirebaseDatabaseModel.kt","listCarerReviews() Error: $error")
                    }
                })
            }
        }

        // Función que lista todos los servicios que ha solicitado un dueño (funciona exactamente igual que listCarerServices):
        suspend fun listOwnerServices(ownerId: String?): HashMap<String, Service>{
            return suspendCoroutine { continuation ->
                val ownerServices = HashMap<String, Service>()
                if(ownerId!= null){
                    val carerServicesRef = databaseRef.child("services").orderByChild("ownerUid").equalTo(ownerId)
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
                            Log.d("FirebaseDatabaseModel.kt","listOwnerServices() Error: $error")
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
                // Inicializamos un HashMap vacío que guardará las imágenes (su ID y su URL):
                val carerPics = HashMap<String, String>()
                if (userId != null){
                    // La referencia está dentro de la colección de cuidadores, en el documento cuidador y en el atributo "imagenes":
                    val carerPicsRef = databaseRef.child("carers").child(userId).child("pics")

                    // Recorremos todos los valores y vamos guardando los no nulos en el HashMap:
                    carerPicsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (picSnapshot in snapshot.children){
                                val picId = picSnapshot.key
                                val picUrl = picSnapshot.getValue<String>()
                                if (picId != null && picUrl != null){
                                    carerPics[picId] = picUrl
                                }
                            }
                            // El bucle ha acabado y la ejecución puede reanudarse:
                            continuation.resume(carerPics)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","listCarerPics() Error: $error")
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
                    // La referencia está dentro de la colección dueños en el documento del dueño y en el atributo de pets:
                    val ownerPetsRef = databaseRef.child("owners").child(userId).child("pets")

                    // Como es habtual, recorremos todos los objetos y los guardamos si no son nulos:
                    ownerPetsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot){
                            for (petSnapshot in snapshot.children){
                                val petId = petSnapshot.key
                                val pet = petSnapshot.getValue<Pet>()
                                if(petId != null && pet != null){
                                    petMap[petId] = pet
                                }
                            }
                            // Ya ha finalizado el bucle y tenemos un HashMap de mascotas. La ejecución puede continuar:
                            continuation.resume(petMap)
                        }
                        override fun onCancelled(error: DatabaseError){
                            Log.d("FirebaseDatabaseModel.kt","listPets() Error: $error")
                        }
                    })
                }
            }
        }

        // Función que actualiza el rating de un cuidador cuando es llamada (cuando se realiza una nueva review):
        fun updateCarerRatings(userId: String?){
            if (userId != null){

                // Establecemos la referencia dentro de la colección cuidadores, en el documento del cuidador y el atributo de opiniones:
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
                                // Vamos sumando el rating total y el numero de reviews:
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
                        Log.d("FirebaseDatabaseModel.kt","updateCarerRatings() Error: $error")
                    }
                })
            }
        }

        // Función que lista la mascota específica de un dueño:
        suspend fun listSinglePet(userId: String?, petUid: String?): Pet? {
            return suspendCoroutine { continuation ->
                if(userId != null && petUid != null){

                    // Referencia que apunta a la colección dueños y a la mascota dentro del cuidador cuyo ID se especifica:
                    val petRef = databaseRef.child("owners").child(userId).
                    child("pets").child(petUid)

                    // Si existe el objeto la ejecución continúa y devuelve el objeto tipo Mascota:
                    petRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val pet = snapshot.getValue<Pet>()
                                continuation.resume(pet)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","listSinglePet() Error: $error")
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
                            Log.d("FirebaseDatabaseModel.kt","getCarerFromFirebase() Error: $error")
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
                            Log.d("FirebaseDatabaseModel.kt","GetOwnerFromFirebase() Error: $error")
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

                    // Comprobamos si el usuario es un dueño...
                    ownersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // Existe, luego es un dueño. Extraemos objeto y continuamos la ejecución:
                                val user = snapshot.getValue<Owner>()
                                continuation.resume(user)
                            } else {
                                // En caso contrario será un cuidador. Por lo que extraeremos el objeto que le toque:
                                carersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(snapshot.exists()){
                                            val user = snapshot.getValue<Carer>()
                                            continuation.resume(user)
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Log.d("FirebaseDatabaseModel.kt","getUserFromFirebase(Carer) Error: $error")
                                    }
                                })
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("FirebaseDatabaseModel.kt","getUserFromFirebase(Owner) Error: $error")
                        }
                    })
                }
            }
        }
    }
}