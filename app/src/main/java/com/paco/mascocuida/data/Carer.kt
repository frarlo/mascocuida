package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Carer(
    private val uid: String? = null,
    private val role: String? = null,
    private val name: String? = null,
    private val lastname: String? = null,
    private val location: String? = null,
    private val pic: String? = null,
    private val email: String? = null,
    private val rating: Double? = null,
    private val aboutMe: String? = null,
    private val pics: HashMap<String,String>? = null,
    private val reviews: HashMap<String,Review>? = null
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

    fun getRating(): Double?{
        return rating
    }

    fun getAboutMe(): String?{
        return aboutMe
    }

    fun getPics(): HashMap<String, String>?{
        return pics
    }

    fun getReviews(): HashMap<String,Review>?{
        return reviews
    }
}


data class Review(
    private val rating: Int? = null,
    private val opinion: String? = null
){
    fun getRating(): Int?{
        return rating
    }

    fun getOpinion(): String?{
        return opinion
    }
}