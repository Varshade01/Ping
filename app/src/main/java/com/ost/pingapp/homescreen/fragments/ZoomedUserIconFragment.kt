package com.khrd.pingapp.homescreen.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import coil.compose.rememberAsyncImagePainter


class ZoomedUserIconFragment : DialogFragment() {

    private val args: ZoomedUserIconFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setBackgroundEffect()

        return ComposeView(requireContext()).apply {
            setContent {
                ZoomInIconDialog()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun setBackgroundEffect() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            dialog?.window?.attributes?.blurBehindRadius = 8
        }
    }


    @Composable
    fun ZoomInIconDialog() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1F)
                .padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(args.photoURL),
                contentDescription = "User profile image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}