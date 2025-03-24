package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.pings.RecurringTime
import com.khrd.pingapp.data.users.DatabaseUser

data class SendPingDialogState(
    val listOfGroups: List<DatabaseGroup> = listOf(),
    val usersInGroup: List<DatabaseUser> = listOf(),
    val listOfChips: List<ReceiverChip> = listOf(),
    val groupName: String = "",
    val emoji:String = "",
    val isGroupChecked: Boolean = false,
    val isScheduleChecked: Boolean = false,
    val isLoading: Boolean = false,
    val recursion: RecurringTime = RecurringTime.NO_REPEAT
)

data class ReceiverChip(val id: String?, val name: String?, val url: String?)
