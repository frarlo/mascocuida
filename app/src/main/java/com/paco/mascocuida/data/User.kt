package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties

// Constructor de usuarios básico - tiene en cuenta los datos básicos. Rol, información personal, etc.

@IgnoreExtraProperties
data class User(
    private val userUid: String? = null,
    private val userRole: String? = null,
    private val userName: String? = null,
    private val userLastname: String? = null,
    private val userLocation: String? = null,
    private val userPic: String? = null,
    private val userEmail: String? = null
) {
    fun getUserUid(): String?{
        return userUid
    }

    fun getUserRole(): String? {
        return userRole
    }

    fun getUserName(): String?{
        return userName
    }

    fun getUserLastname(): String?{
        return userLastname
    }

    fun getUserLocation(): String?{
        return userLocation
    }
    fun getUserPic(): String?{
        return userPic
    }
    fun getEmail(): String?{
        return userEmail
    }
}
