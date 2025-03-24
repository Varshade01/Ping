package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.homescreen.states.SendScheduledPingsAfterRebootState

interface SendScheduledPingsAfterRebootUseCase {
    fun sendScheduledPings(pings: List<DatabasePing>, callback: (SendScheduledPingsAfterRebootState) -> Unit)
}