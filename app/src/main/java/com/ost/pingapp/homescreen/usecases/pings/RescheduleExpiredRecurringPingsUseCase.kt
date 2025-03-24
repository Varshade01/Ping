package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.homescreen.states.RescheduleExpiredRecurringPingsState

interface RescheduleExpiredRecurringPingsUseCase {
    fun rescheduleExpiredRecurringPings(pings: List<DatabasePing>, callback: (RescheduleExpiredRecurringPingsState) -> Unit)
}