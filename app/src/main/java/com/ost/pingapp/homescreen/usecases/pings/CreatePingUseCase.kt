package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.homescreen.states.CreatePingState

interface CreatePingUseCase {
    fun createPing(ping: PingData, callback: (CreatePingState) -> Unit)
}