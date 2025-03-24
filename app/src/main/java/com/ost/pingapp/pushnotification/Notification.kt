package com.khrd.pingapp.pushnotification

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class PushNotification(
    val registration_ids: List<String>,
    val data: PushData,
    val android: Map<String, String> = mapOf("priority" to "high")
) : Parcelable