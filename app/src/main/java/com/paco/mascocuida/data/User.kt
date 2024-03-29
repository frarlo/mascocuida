package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties

// Constructor de usuarios básico - tiene en cuenta los datos básicos. Rol, información personal, etc.

@IgnoreExtraProperties
data class User(
    private val uid: String? = null,
    private val role: String? = null,
    private val name: String? = null,
    private val lastname: String? = null,
    private val location: String? = null,
    private val pic: String? = null,
    private val email: String? = null
) {
    fun getUid(): String?{
        return uid
    }

    fun getRole(): String? {
        return role
    }

    fun getName(): String?{
        return name
    }

    fun getLastname(): String?{
        return lastname
    }

    fun getLocation(): String?{
        return location
    }
    fun getPic(): String?{
        return pic
    }
    fun getEmail(): String?{
        return email
    }
}
