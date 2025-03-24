package com.khrd.pingapp.utils.imageLoader

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes

interface ImageLoader {
    fun loadImage(imageUrl: String?, targetSource: ImageView, @DrawableRes defaultImage: Int)
    fun loadImage(bitmap: Bitmap?, targetSource: ImageView, @DrawableRes defaultImage: Int)
}