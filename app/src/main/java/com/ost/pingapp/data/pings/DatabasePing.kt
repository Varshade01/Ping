package com.khrd.pingapp.data.pings

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.khrd.pingapp.data.users.Online
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class DatabasePing(
    val id: String = "",
    val timestamp: Long? = null,
    val from: HashMap<String, String> = hashMapOf(),
    val receivers: HashMap<String, String> = hashMapOf(),
    val message: String = "",
    val views: HashMap<String, String> = hashMapOf(),
    val groupId: String? = null,
    val scheduledTime: Long? = null,
    val groupFrom: String = "",
    var online: Online? = null,
    val recurringTime: RecurringTime = RecurringTime.NO_REPEAT
) : Parcelable
