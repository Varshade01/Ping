package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.data.pings.RecurringTime

sealed class SendPingDialogSideEffect {
    data class SendPingFailureEffect(val error: SendPingError) : SendPingDialogSideEffect()
    object SendPingSuccessEffect : SendPingDialogSideEffect()
    data class SchedulePingEffect(val pingId: String, val scheduledTime: Long, val recurringTime: RecurringTime) : SendPingDialogSideEffect()
    data class SendPingOfflineEffect(val pingData: PingData) : SendPingDialogSideEffect()
}

enum class SendPingError {
    NO_RECEIVER,
    PING_CREATION_FAILURE,
    SCHEDULED_FOR_PAST
}