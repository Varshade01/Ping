package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter

import android.os.Parcelable
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.pings.RecurringTime
import com.khrd.pingapp.data.users.Online
import com.khrd.pingapp.homescreen.adapter.UserItem
import kotlinx.parcelize.Parcelize

sealed class DisplayablePingItem()

@Parcelize
data class SentPingItem(
    val receiver: List<UserItem> = emptyList(),
    val date: Long = 0,
    val emoji: String = "",
    val groupId: String? = null,
    val views: List<String> = emptyList(),
    var groupFrom: DatabaseGroup? = null,
    var online: Online? = null
) : DisplayablePingItem(), Parcelable

fun SentPingItem.isGroupPing(): Boolean {
    return this.groupId != null
}

data class SentPingScheduledItem(
    val receiver: List<UserItem> = emptyList(),
    val scheduledDate: Long = 0,
    val emoji: String = "",
    val pingId: String = "",
    val groupId: String? = null,
    val timestamp: Long = 0,
    var groupFrom: DatabaseGroup? = null,
    var online: Online? = null,
    var recurringTime: RecurringTime
) : DisplayablePingItem()

data class ReceivedPingItem(
    val id: String = "",
    val userItem: UserItem?,
    val date: Long = 0,
    val emoji: String = "",
    val isGroupPing: Boolean = false,
    val seen: Boolean = false,
    val isSenderDeleted: Boolean = false,
    var groupFrom: DatabaseGroup? = null,
) : DisplayablePingItem()

data class SentPingScheduledHeader(var expanded: Boolean) : DisplayablePingItem()

object SentPingScheduledEmptyMessage : DisplayablePingItem()

object SentPingDivider : DisplayablePingItem()
