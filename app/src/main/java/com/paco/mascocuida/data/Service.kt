package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties

/*
* Esta clase (data-class en Kotlin) es un constructor de un objeto tipo "Servicio". Dado cómo trabajamos con los servicios
* y sus actualizaciones, hemos optado por no incluir métodos get/set.
*/

@IgnoreExtraProperties
data class Service(
    // El servicio de cuidado actúa como una tabla intermedia donde ownerUid y carerUid serían la clave primaria.
    val ownerUid: String? = null,
    val carerUid: String? = null,
    val pet: Pet? = null,
    val date: String? = null,
    val time: String? = null,
    val information: String? = null,
    var status: String? = null
)
