package com.paco.mascocuida.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Carer(
    private val uid: String? = null,
    private val role: String? = null,
    private var name: String? = null,
    private var lastname: String? = null,
    private var location: String? = null,
    private var pic: String? = null,
    private val email: String? = null,
    private var rating: Double? = null,
    private var aboutMe: String? = null,
    private var pics: HashMap<String,String>? = null,
    private var reviews: HashMap<String,Review>? = null
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

    fun getRating(): Double?{
        return rating
    }

    fun setRating(rating: Double){
        this.rating = rating
    }

    fun getAboutMe(): String?{
        return aboutMe
    }

    fun setAboutMe(aboutMe: String){
        this.aboutMe = aboutMe
    }

    fun getPics(): HashMap<String, String>?{
        return pics
    }

    fun setPics(pics: HashMap<String,String>){
        this.pics = pics
    }

    fun getReviews(): HashMap<String,Review>?{
        return reviews
    }

    fun setReviews(reviews: HashMap<String,Review>){
        this.reviews = reviews
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