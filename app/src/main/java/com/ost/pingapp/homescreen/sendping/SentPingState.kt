package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingScheduledItem

data class SentPingsState(
    val sentItems: List<SentPingItem> = listOf(),
    val scheduledItems: List<SentPingScheduledItem> = listOf(),
    val showScheduled: Boolean = false,
)


sealed class BaseSentPingsError {
    object SentPingsError : BaseSentPingsError()
    object ScheduledPingsError : BaseSentPingsError()
}

data class StatusDialogData(
    val sentPingItem: SentPingItem,
    val dataBaseUser: DatabaseUser,
)
