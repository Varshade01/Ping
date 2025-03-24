package com.khrd.pingapp.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

const val APP_PLAY_MARKET_LINK = "https://play.google.com/store/apps/details?id=com.khrd.pingapp"
const val FIREBASE_APP_LINK = "https://khrd.page.link/"
const val PACKAGE_NAME = "com.khrd.pingapp"
const val GROUPID_PREFIX = "&groupid="
const val TAG = "pingapp"

interface FirebaseDynamicLinkAPI {
    fun generateLongDynamicLink(groupId: String): Uri

    fun generateShortDynamicLink(groupId: String, callback: (uri: Uri?) -> Unit)
}

class FirebaseDynamicLink : FirebaseDynamicLinkAPI {

    override fun generateLongDynamicLink(groupId: String): Uri {

        val uri = FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(Uri.parse(APP_PLAY_MARKET_LINK + GROUPID_PREFIX + groupId))
            .setDomainUriPrefix(FIREBASE_APP_LINK)
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(PACKAGE_NAME)
                    // so far we don't have app web page user redirected to the Playmarket App page
                    .setFallbackUrl(Uri.parse(APP_PLAY_MARKET_LINK))
                    .build()
            )
            .buildDynamicLink()
            .uri

        Log.i(TAG, "Created long link:$uri")
        return uri
    }

    override fun generateShortDynamicLink(groupId: String, callback: (uri: Uri?) -> Unit) {

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLongLink(generateLongDynamicLink(groupId))
            .buildShortDynamicLink()
            .addOnSuccessListener { task ->
                // Short link created
                Log.i(TAG, "Created short link:${task.shortLink.toString()}")
                Log.i(TAG, "Short link creation warnings:${task.warnings}")
                task.shortLink?.let { callback(it) }
            }
            .addOnFailureListener {
                Log.e(TAG, it.stackTraceToString())
                callback(null)
            }
        return
    }
}