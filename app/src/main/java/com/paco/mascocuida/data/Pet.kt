package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties

/*
* Esta clase (una data-class de Kotlin) es un constructor de un objeto tipo Mascota (pet). Incluye los getters/setters
* para incluir una capa más de abstracción en nuestra lógica y un método añadido para mostrar cada objeto instanciado
* por su nombre.
*/
@IgnoreExtraProperties
data class Pet(
    private val ownerUid: String? = null,
    private val petUid: String? = null,
    private var name: String? = null,
    private var species: String? = null,
    private var size: String? = null,
    private var age: String? = null,
    private var gender: String? = null,
    private var likesDogs: Boolean? = false,
    private var likesCats: Boolean? = false,
    private var isSterilised: Boolean? = false
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

    fun setName(name: String){
        this.name = name
    }

    fun getSpecies(): String? {
        return species
    }

    fun setSpecies(species: String){
        this.species = species
    }

    fun getSize(): String? {
        return size
    }

    fun setSize(size: String){
        this.size = size
    }

    fun getAge(): String? {
        return age
    }

    fun setAge(age: String){
        this.age = age
    }

    fun getGender(): String? {
        return gender
    }

    fun setGender(gender: String){
        this.gender = gender
    }

    fun getLikesDogs(): Boolean? {
        return likesDogs
    }

    fun setLikesDogs(likesDogs: Boolean){
        this.likesDogs = likesDogs
    }

    fun getLikesCats(): Boolean? {
        return likesCats
    }

    fun setLikesCats(likesCats: Boolean){
        this.likesCats = likesCats
    }

    fun getIsSterilised(): Boolean? {
        return isSterilised
    }

    fun setIsSterilised(isSterilised: Boolean){
        this.isSterilised = isSterilised
    }
}

