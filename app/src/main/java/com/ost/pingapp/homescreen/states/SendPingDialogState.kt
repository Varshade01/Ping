package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.users.DatabaseUser

sealed class SendPingDialogState
data class SendPingDialogSuccess(val user: String?, val group: String?) : SendPingDialogState()
object SendPingDialogFailure : SendPingDialogState()
