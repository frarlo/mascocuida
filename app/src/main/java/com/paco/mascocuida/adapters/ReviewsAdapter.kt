package com.paco.mascocuida.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.paco.mascocuida.R
import com.paco.mascocuida.data.Review

/*
* Esta clase es un adaptador que nos permite mostrar al usuario, en este caso específico, un listado de Reviews en una vista
* de RecyclerView.
*/
class ReviewsAdapter(private val reviewsMap: HashMap<String, Review>): RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {

    // Clase principal del Adaptador. Representa cada item dentro de la lista (o Mapa):
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        // Inicializamos todos los elementos de la vista:
        private var ownerName: TextView = view.findViewById(R.id.recycler_review_author)
        private var ownerReview: TextView = view.findViewById(R.id.recycler_review_opinion)
        private var starTwo: ImageView = view.findViewById(R.id.second_star)
        private var starThree: ImageView = view.findViewById(R.id.third_star)
        private var starFour: ImageView = view.findViewById(R.id.fourth_star)
        private var starFive: ImageView = view.findViewById(R.id.fifth_star)

        // Este método bindea el objeto a la vista:
        fun bindReview(review: Review?){

            // Extraemos el autor de la review:
            ownerName.text = review?.getAuthor()
            // Comprobamos si hay una opinión escrita.
            if(review?.getOpinion().isNullOrEmpty()){
                // Si no la hay no mostraremos el TextView:
                ownerReview.isVisible = false
            }
            // Haya texto o no lo extraemos y lo mostramos por pantalla:
            ownerReview.text = review?.getOpinion()

            // Finalmente extraemos el rating:
            val rating = review?.getRating()

            // Switch que maneja cuántas estrellas se mostrarán según el rating del servicio:
            when(rating){
                1 -> {
                    // No hace nada ya que de forma predeterminada se muestra una estrella y las otras cuatro vacías:
                }
                2 -> {
                    starTwo.setImageResource(R.drawable.rating_star)
                }
                3 -> {
                    starTwo.setImageResource(R.drawable.rating_star)
                    starThree.setImageResource(R.drawable.rating_star)
                }
                4 -> {
                    starTwo.setImageResource(R.drawable.rating_star)
                    starThree.setImageResource(R.drawable.rating_star)
                    starFour.setImageResource(R.drawable.rating_star)
                }
                5 -> {
                    starTwo.setImageResource(R.drawable.rating_star)
                    starThree.setImageResource(R.drawable.rating_star)
                    starFour.setImageResource(R.drawable.rating_star)
                    starFive.setImageResource(R.drawable.rating_star)
                }
            }
        }
    }

    // Método predeterminado del Adaptador que retorna la vista creada por el adaptador:
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_layout, parent, false)
        return ViewHolder(view)
    }

    // Método predeterminado del Adaptador que retorna el tamaño del HashMap de Reviews:
    override fun getItemCount(): Int {
        return reviewsMap.size
    }

    // Método predeterminado que bindeea cada item a la vista llamando al método:
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewId = reviewsMap.keys.toList()[position]
        val review = reviewsMap[reviewId]
        holder.bindReview(review)
    }

}