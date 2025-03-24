package com.khrd.pingapp.homescreen.adapter

import android.os.Parcelable
import android.text.Spannable
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.Online
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class UserItem(
    var fullname: @RawValue Spannable?,
    var job: String? = "",
    var photoURL: String? = "",
    var userId: String? = "",
    var groups: List<DatabaseGroup?>,
    var isOnline: Online? = null,
    var muted: Boolean = false,
    var isDeleted: Boolean? = null,
    var isHide: Boolean = false,
) : Parcelable
