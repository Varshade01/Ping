package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.data.pings.RecurringTime

sealed class SendPingDialogIntent {
    data class GroupChangedIntent(val groupId: String) : SendPingDialogIntent()
    data class SendPingReceiverAsGroupIntent(val isGroupPing: Boolean) : SendPingDialogIntent()
    data class SendPingReceiverAddedIntent(val receiversId: List<String>) : SendPingDialogIntent()
    data class SendPingDatePickedIntent(val year: Int, val month: Int, val day: Int) : SendPingDialogIntent()
    data class SendPingTimePickedIntent(val hour: Int, val minute: Int) : SendPingDialogIntent()
    data class SendPingRecurringTimePickedIntent(val recurringTime: RecurringTime) : SendPingDialogIntent()
    object UnschedulePingIntent : SendPingDialogIntent()
    data class SendPingEmojiPickedIntent(val emoji: String) : SendPingDialogIntent()
    data class SendPingReceiverRemovedIntent(val id: String?) : SendPingDialogIntent()
    object SendPingIntent : SendPingDialogIntent()
}