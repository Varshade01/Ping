package com.khrd.pingapp.pushnotification

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class PushData(val type: String, val ping_id: String) : Parcelable