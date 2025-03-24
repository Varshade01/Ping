package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.homescreen.states.RewriteScheduledPingsState

interface RewriteScheduledPingsUseCase {
    fun rewriteScheduledPings(scheduledPings: List<DatabasePing>, callback: (RewriteScheduledPingsState) -> Unit)
}