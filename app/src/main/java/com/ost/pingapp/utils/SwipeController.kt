package com.khrd.pingapp.utils

import android.animation.ObjectAnimator
import android.content.res.Resources
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView


class SwipeController : Callback() {

    private val muteButtonWidth = 66
    private val limitScrollX = muteButtonWidth.toDp()
    private var currentScrollX = 0
    private var currentScrollXWhenInActive = 0
    private var initXWhenInActive = 0f
    private var firstInActive = false
    private var lastViewHolder: RecyclerView.ViewHolder? = null

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = 0
        val swipeFlags = LEFT or RIGHT
        if (lastViewHolder != viewHolder) {
            collapse()
            lastViewHolder = viewHolder
        }
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return Integer.MAX_VALUE.toFloat()
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Integer.MAX_VALUE.toFloat()
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ACTION_STATE_SWIPE) {
            if (dX == 0f) {
                currentScrollX = viewHolder.itemView.scrollX
                firstInActive = true
            }

            if (isCurrentlyActive) {
                var scrollOffset = currentScrollX + (-dX).toInt()
                if (scrollOffset > limitScrollX) {
                    scrollOffset = limitScrollX
                } else if (scrollOffset < 0) {
                    scrollOffset = 0
                }

                viewHolder.itemView.scrollTo(scrollOffset, 0)

            } else {
                if (firstInActive) {
                    firstInActive = false
                    currentScrollXWhenInActive = viewHolder.itemView.scrollX
                    initXWhenInActive = dX
                }

                if (viewHolder.itemView.scrollX < limitScrollX) {
                    viewHolder.itemView.scrollTo((currentScrollXWhenInActive * dX / initXWhenInActive).toInt(), 0)
                }
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder.itemView.scrollX > limitScrollX) {
            viewHolder.itemView.scrollTo(limitScrollX, 0)
        } else if (viewHolder.itemView.scrollX < 0) {
            viewHolder.itemView.scrollTo(0, 0)
        }
    }

    private fun Int.toDp(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun collapse() {
        if (lastViewHolder != null) {
            ObjectAnimator.ofInt(lastViewHolder?.itemView, "scrollX", lastViewHolder?.itemView?.scrollX ?: 0, 0)
                .setDuration(250)
                .start()
        }
    }
}
