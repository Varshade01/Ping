package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.homescreen.states.*
import javax.inject.Inject

class SendScheduledPingsAfterRebootUseCaseImpl @Inject constructor(
    val rewriteScheduledPingsUseCase: RewriteScheduledPingsUseCase
) : SendScheduledPingsAfterRebootUseCase {
    override fun sendScheduledPings(pings: List<DatabasePing>, callback: (SendScheduledPingsAfterRebootState) -> Unit) {
        rewriteScheduledPingsUseCase.rewriteScheduledPings(pings) {
            when (it) {
                is RewriteScheduledPingsSuccess -> {
                    callback(SendScheduledPingsAfterRebootSuccess(it.databasePing))
                }
                is RewriteScheduledPingsFailure -> {
                    callback(SendScheduledPingsAfterRebootFailure())
                }
            }
        }
    }
}
