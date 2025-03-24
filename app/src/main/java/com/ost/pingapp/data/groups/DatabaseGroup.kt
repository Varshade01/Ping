package com.khrd.pingapp.data.groups

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class DatabaseGroup(
    var id: String = "",
    var name: String? = "",
    var invitationLink: String? = "",
    var photoURL: String? = null,
) : Parcelable