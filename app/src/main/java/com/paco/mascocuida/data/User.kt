package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties

// Constructor de usuarios básico - tiene en cuenta los datos básicos. Rol, información personal, etc.

@IgnoreExtraProperties
data class User(
    val userUid: String,
    val userRole: String? = null,
    val userName: String? = null,
    val userLastname: String? = null,
    val userLocation: String? = null,
    val userPic: String? = null,
    val userEmail: String? = null,
)
