package com.paco.mascocuida.data

data class Owner(
    private val uid: String? = null,
    private val role: String? = null,
    private var name: String? = null,
    private var lastname: String? = null,
    private var location: String? = null,
    private var pic: String? = null,
    private var email: String? = null,
    private val pets: HashMap<String, Pet>? = null
){
    fun getUid(): String?{
        return uid
    }

    fun getRole(): String? {
        return role
    }

    fun getName(): String?{
        return name
    }

    fun setName(name: String){
        this.name = name
    }

    fun getLastname(): String?{
        return lastname
    }

    fun setLastname(lastname: String){
        this.lastname = lastname
    }

    fun getLocation(): String?{
        return location
    }

    fun setLocation(location: String){
        this.location = location
    }

    fun getPic(): String?{
        return pic
    }

    fun setPic(pic: String){
        this.pic = pic
    }

    fun getEmail(): String?{
        return email
    }

    fun setEmail(email: String){
        this.email = email
    }

    fun getPets(): HashMap<String, Pet>?{
        return pets
    }

}