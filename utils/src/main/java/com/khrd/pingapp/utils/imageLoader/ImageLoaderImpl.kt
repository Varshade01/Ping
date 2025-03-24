package com.khrd.pingapp.utils.imageLoader

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide

class ImageLoaderImpl(val context: Context) : ImageLoader {
    override fun loadImage(imageUrl: String?, targetSource: ImageView, @DrawableRes defaultImage: Int) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .circleCrop()
                .into(targetSource)
        } else {
            targetSource.setImageResource(defaultImage)
        }
    }

    override fun loadImage(bitmap: Bitmap?, targetSource: ImageView, @DrawableRes defaultImage: Int) {
        if (bitmap != null) {
            Glide.with(context)
                .load(bitmap)
                .circleCrop()
                .into(targetSource)
        } else {
            targetSource.setImageResource(defaultImage)
        }
    }
}