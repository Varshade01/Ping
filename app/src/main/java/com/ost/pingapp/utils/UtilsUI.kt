package com.khrd.pingapp.utils

import android.view.MotionEvent
import android.view.View
import androidx.navigation.NavController
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun View.showZoomedImageOnTouch(
    navController: NavController,
    photoURLs: List<String>
) {
    if (photoURLs.isNotEmpty()) {
        var job: Job? = null
        var isDialogOpened = false

        setOnTouchListener { view, motionEvent ->
            val width = view.width
            val height = view.height
            val halfWidth = width / 2
            val halfHeight = height / 2

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(1.1f).scaleY(1.1f).duration = 200

                    // Cancel any existing job before starting a new one
                    job?.cancel()

                    val x = motionEvent.x
                    val y = motionEvent.y

                    val clickedPart: PicturePart = when (photoURLs.size) {
                        // Handle the case when there are only one picture
                        1 -> PicturePart.FULL_ICON

                        2 -> {
                            // Handle the case when there are only two pictures
                            if (x < halfWidth && y < height) PicturePart.LEFT_PART
                            else PicturePart.RIGHT_PART
                        }

                        3 -> {
                            // Handle the case when there are three pictures
                            when {
                                x < halfWidth && y < halfHeight -> PicturePart.TOP_LEFT
                                x >= halfWidth && y < halfHeight -> PicturePart.TOP_RIGHT
                                else -> PicturePart.BOTTOM_LEFT
                            }
                        }

                        4 -> {
                            // Handle the case when there are four pictures
                            when {
                                x < halfWidth && y < halfHeight -> PicturePart.TOP_LEFT
                                x >= halfWidth && y < halfHeight -> PicturePart.TOP_RIGHT
                                x < halfWidth && y >= halfHeight -> PicturePart.BOTTOM_LEFT
                                else -> PicturePart.BOTTOM_RIGHT
                            }
                        }

                        else -> throw RuntimeException("Invalid number of pictures")
                    }

                    job = CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        //gets an element from the photoURLs list at a given index
                        val selectedPhotoURLbyIndex = photoURLs.getOrNull(partIndexMap[clickedPart] ?: -1)

                        selectedPhotoURLbyIndex?.let {
                            val action = HomescreenNavGraphDirections.showZoomedUserIconDialog(photoURL = it)
                            navController.navigateSafe(action)
                            isDialogOpened = true
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    view.animate().scaleX(1.0f).scaleY(1.0f).duration = 200

                    // Cancel the job if it's still running
                    job?.cancel()
                    job = null

                    if (isDialogOpened) {
                        navController.popBackStack()
                        isDialogOpened = false
                    }
                }
            }

            view.performClick()
            true
        }
    } else return
}

enum class PicturePart {
    FULL_ICON,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    LEFT_PART,
    RIGHT_PART,
}

val partIndexMap = mapOf(
    PicturePart.FULL_ICON to 0,
    PicturePart.TOP_LEFT to 0,
    PicturePart.TOP_RIGHT to 1,
    PicturePart.BOTTOM_LEFT to 2,
    PicturePart.BOTTOM_RIGHT to 3,
    PicturePart.LEFT_PART to 0,
    PicturePart.RIGHT_PART to 1
)

fun View.showZoomedImageOnTouch(
    navController: NavController,
    photoURL: String
) {
    showZoomedImageOnTouch(navController, listOf(photoURL))
}


