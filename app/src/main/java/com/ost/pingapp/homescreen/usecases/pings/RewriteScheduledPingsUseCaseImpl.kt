package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.data.pings.RecurringTime
import com.khrd.pingapp.homescreen.states.*
import javax.inject.Inject

class RewriteScheduledPingsUseCaseImpl @Inject constructor(
    val createPingUseCase: CreatePingUseCase,
    val deleteScheduledPingsUseCase: DeleteScheduledPingsUseCase
) : RewriteScheduledPingsUseCase {
    override fun rewriteScheduledPings(scheduledPings: List<DatabasePing>, callback: (RewriteScheduledPingsState) -> Unit) {
        val overduePings =
            scheduledPings.filter { ping -> ping.scheduledTime!! <= System.currentTimeMillis() && ping.recurringTime == RecurringTime.NO_REPEAT }
                .map {
                    PingData(
                        pingId = it.id,
                        from = it.from.keys.toList()[0],
                        receivers = it.receivers.keys.toList(),
                        emoji = it.message,
                        groupId = it.groupId,
                        timestamp = it.scheduledTime,
                        groupFrom = it.groupFrom,
                        recurringTime = it.recurringTime
                    )
                }
        overduePings.forEach { ping ->
            createPingUseCase.createPing(ping) { createState ->
                when (createState) {
                    is CreatePingSuccess -> {
                        deleteScheduledPingsUseCase.deleteScheduledPings(ping.pingId!!) { deleteState ->
                            when (deleteState) {
                                is DeleteScheduledPingSuccess -> {
                                    callback(RewriteScheduledPingsSuccess(createState.pings[0]))
                                }
                                is DeleteScheduledPingFailure -> {
                                    callback(RewriteScheduledPingsFailure())
                                }
                                else -> RewriteScheduledPingsFailure()
                            }
                        }
                    }
                    is CreatePingFailure -> {
                        callback(RewriteScheduledPingsFailure())
                    }
                    CreatePingOfflineState -> RewriteScheduledPingsFailure()
                }
            }
        }
    }
}
