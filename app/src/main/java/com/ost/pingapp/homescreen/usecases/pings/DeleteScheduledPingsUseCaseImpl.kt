package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.data.pings.PingOfflineState
import com.khrd.pingapp.data.pings.PingStateFailure
import com.khrd.pingapp.data.pings.PingStateSuccess
import com.khrd.pingapp.homescreen.states.DeleteScheduledPingFailure
import com.khrd.pingapp.homescreen.states.DeleteScheduledPingOffline
import com.khrd.pingapp.homescreen.states.DeleteScheduledPingState
import com.khrd.pingapp.homescreen.states.DeleteScheduledPingSuccess
import com.khrd.pingapp.repository.pings.PingsRepository
import javax.inject.Inject

class DeleteScheduledPingsUseCaseImpl @Inject constructor(
    private val repository: PingsRepository,
) : DeleteScheduledPingsUseCase {
    override fun deleteScheduledPings(id: String, callback: (DeleteScheduledPingState) -> Unit) {
        repository.deleteScheduledPing(id) {
            when (it) {
                is PingStateSuccess -> {
                    callback(DeleteScheduledPingSuccess())
                }
                is PingStateFailure -> {
                    callback(DeleteScheduledPingFailure())
                }
                PingOfflineState -> {
                    callback(DeleteScheduledPingOffline())
                    repository.removeScheduledPingFromDB(id)
                }
            }
        }
    }
}