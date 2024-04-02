package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Pet(
    private val ownerUid: String? = null,
    private val petUid: String? = null,
    private val name: String? = null,
    private val species: String? = null,
    private val size: String? = null,
    private val age: String? = null,
    private val gender: String? = null,
    private val likesDogs: Boolean? = false,
    private val likesCats: Boolean? = false,
    private val isSterilised: Boolean? = false
) {

    // Función añadida para mostrar cada objeto instanciado de mascota simplemente por su nombre:
    override fun toString(): String {
        return name!!
    }

    fun getOwnerUid(): String? {
        return ownerUid
    }

    fun getPetUid(): String? {
        return petUid
    }

    fun getName(): String? {
        return name
    }

    fun getSpecies(): String? {
        return species
    }

    fun getSize(): String? {
        return size
    }

    fun getAge(): String? {
        return age
    }

    fun getGender(): String? {
        return gender
    }

    fun getLikesDogs(): Boolean? {
        return likesDogs
    }

    fun getLikesCats(): Boolean? {
        return likesCats
    }

    fun getIsSterilised(): Boolean? {
        return isSterilised
    }
}

