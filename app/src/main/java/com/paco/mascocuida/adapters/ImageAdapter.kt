package com.paco.mascocuida.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide

// Basado en https://www.geeksforgeeks.org/android-image-slider-using-viewpager-in-kotlin/
class ImageAdapter(private val imagesList: HashMap<String,String>) : PagerAdapter()  {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val imageView = ImageView(container.context)

        val imageUrl = imagesList.entries.elementAt(position).value

        Glide.with(container.context).load(imageUrl).into(imageView)

        container.addView(imageView)

        return imageView
    }

    override fun isViewFromObject(view: View, image: Any): Boolean {
        return view == image
    }

    override fun getCount(): Int {
        return imagesList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, image: Any) {
        container.removeView(image as View)
    }
}