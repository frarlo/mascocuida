package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class Service(
    val ownerUid: String? = null,
    val carerUid: String? = null,
    val petUid: String? = null,
    val date: Date? = null,
    val time: Date? = null,
    var status: String? = null
)
