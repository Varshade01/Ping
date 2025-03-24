package com.khrd.pingapp.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout

fun expand(v: View) {
    if (v.visibility == View.VISIBLE) return
    val durations: Long
    val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(
        (v.parent as View).width,
        View.MeasureSpec.EXACTLY
    )
    val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(
        0,
        View.MeasureSpec.UNSPECIFIED
    )
    v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    val targetHeight = v.measuredHeight

    v.layoutParams.height = 1
    v.visibility = View.VISIBLE
    durations = ((targetHeight / v.context.resources
        .displayMetrics.density)).toLong()

    v.alpha = 0.0F
    v.visibility = View.VISIBLE
    v.animate().alpha(1.0F).setDuration(durations).setListener(null)

    val a: Animation = object : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation
        ) {
            v.layoutParams.height =
                if (interpolatedTime == 1f) LinearLayout.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
            v.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = durations
    v.startAnimation(a)
}

fun collapse(v: View) {
    if (v.visibility == View.GONE) return
    val durations: Long
    val initialHeight = v.measuredHeight
    val a: Animation = object : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation
        ) {
            if (interpolatedTime == 1f) {
                v.visibility = View.GONE
            } else {
                v.layoutParams.height =
                    initialHeight - (initialHeight * interpolatedTime).toInt()
                v.requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    durations = (initialHeight / v.context.resources
        .displayMetrics.density).toLong()

    v.alpha = 1.0F
    v.animate().alpha(0.0F).setDuration(durations)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                v.visibility = View.GONE
                v.alpha = 1.0F
            }
        })
    a.duration = durations
    v.startAnimation(a)
}