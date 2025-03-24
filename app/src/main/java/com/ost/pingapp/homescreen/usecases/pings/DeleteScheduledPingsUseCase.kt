package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.homescreen.states.DeleteScheduledPingState

interface DeleteScheduledPingsUseCase {
    fun deleteScheduledPings(id: String, callback: (DeleteScheduledPingState) -> Unit)
}