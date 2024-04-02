package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class Service(
    val ownerUid: String? = null,
    val carerUid: String? = null,
    val pet: Pet? = null,
    val date: String? = null,
    val time: String? = null,
    val information: String? = null,
    var status: String? = null
)
