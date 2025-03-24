package com.khrd.pingapp.data.users

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class DatabaseUser(
    var id: String? = null,
    var email: String? = null,
    var username: String? = null,
    var groups: HashMap<String, String>? = hashMapOf(),
    var job: String? = null,
    var photoURL: String? = null,
    var fcmTokens: HashMap<String, String>? = hashMapOf(),
    var isDeleted: Boolean? = null,
    var online: HashMap<String, Online>? = hashMapOf(),
    var mutedItems: HashMap<String, String>? = hashMapOf(),
    var hideInfo: Boolean = false,
) : Parcelable

@IgnoreExtraProperties
@Parcelize
data class Online(
    var status: Boolean? = false,
    var timestamp: Long = 0,
) : Parcelable