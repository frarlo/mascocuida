package com.paco.mascocuida.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Review

class ReviewsAdapter(private val reviewsMap: HashMap<String, Review>): RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private var ownerName: TextView = view.findViewById(R.id.recycler_review_author)
        private var ownerReview: TextView = view.findViewById(R.id.recycler_review_opinion)
        private var starTwo: ImageView = view.findViewById(R.id.second_star)
        private var starThree: ImageView = view.findViewById(R.id.third_star)
        private var starFour: ImageView = view.findViewById(R.id.fourth_star)
        private var starFive: ImageView = view.findViewById(R.id.fifth_star)

        fun bindReview(review: Review?){

            ownerName.text = review?.getAuthor()
            ownerReview.text = review?.getOpinion()

            val rating = review?.getRating()

            when(rating){
                1 -> {
                    // Does nothing, 1/5 hardcoded
                }
                2 -> {
                    starTwo.setBackgroundResource(R.drawable.rating_star)
                }
                3 -> {
                    starTwo.setBackgroundResource(R.drawable.rating_star)
                    starThree.setBackgroundResource(R.drawable.rating_star)
                }
                4 -> {
                    starTwo.setBackgroundResource(R.drawable.rating_star)
                    starThree.setBackgroundResource(R.drawable.rating_star)
                    starFour.setBackgroundResource(R.drawable.rating_star)
                }
                5 -> {
                    starTwo.setBackgroundResource(R.drawable.rating_star)
                    starThree.setBackgroundResource(R.drawable.rating_star)
                    starFour.setBackgroundResource(R.drawable.rating_star)
                    starFive.setBackgroundResource(R.drawable.rating_star)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reviewsMap.size
    }

    override fun onBindViewHolder(holder: ReviewsAdapter.ViewHolder, position: Int) {
        val reviewId = reviewsMap.keys.toList()[position]
        val review = reviewsMap[reviewId]
        holder.bindReview(review)
    }

}