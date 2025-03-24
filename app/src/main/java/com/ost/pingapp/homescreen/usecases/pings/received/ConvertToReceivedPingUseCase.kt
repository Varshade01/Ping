package com.khrd.pingapp.homescreen.usecases.pings.received

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.homescreen.states.LoadReceivedPingsState

interface ConvertToReceivedPingUseCase {
    fun convertToReceivedPing(pings: List<DatabasePing>, loadFlag: DataLoadFlag, callback: (LoadReceivedPingsState) -> Unit)
}