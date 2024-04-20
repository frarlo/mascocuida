package com.paco.mascocuida.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide

/*
* Esta clase es un adaptador basado en https://www.geeksforgeeks.org/android-image-slider-using-viewpager-in-kotlin/
*  que nos permite mostrar al usuario un listado de imágenes de perfil del cuidador en un Pager. La lógica es ligeramente
*  distinta a otros adaptadores ya que queríamos mostrar las imagenes de forma secuencial de izquierda a derecha:
*/
class ImageAdapter(private val imagesList: HashMap<String,String>) : PagerAdapter()  {

    // Clase principal del Adaptador. Instancia cada item dentro del mapa de imágenes:
    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        // Obtenemos el contenedor de imágenes:
        val imageView = ImageView(container.context)

        // Hacemos que el ImageView se acople a los márgenes:
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Declaramos el tipo de escalado (recortar y centrar):
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Obtenemos la URL de la imagen...
        val imageUrl = imagesList.entries.elementAt(position).value

        // y Glide, con el contenedor, carga la URL dentro del ImageView:
        Glide.with(container.context).load(imageUrl).into(imageView)

        // Añadimos la vista para permitir una paginación:
        container.addView(imageView)

        // Devuelve la vista de imagen:
        return imageView
    }

    // Método predeterminado que especifica si una vista se asocia con un objeto:
    override fun isViewFromObject(view: View, image: Any): Boolean {
        return view == image
    }

    // Método predeterminado que devuelve el tamaño de la lista de imágenes:
    override fun getCount(): Int {
        return imagesList.size
    }

    // Método predeterminado que borra un item dentro de la vista:
    override fun destroyItem(container: ViewGroup, position: Int, image: Any) {
        container.removeView(image as View)
    }

}