package com.khrd.pingapp.homescreen.usecases.pings

import android.util.Log
import com.khrd.pingapp.data.pings.*
import com.khrd.pingapp.di.MainCoroutineScope
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.sendping.SendPushUseCase
import com.khrd.pingapp.homescreen.states.CreatePingFailure
import com.khrd.pingapp.homescreen.states.CreatePingOfflineState
import com.khrd.pingapp.homescreen.states.CreatePingState
import com.khrd.pingapp.homescreen.states.CreatePingSuccess
import com.khrd.pingapp.pushnotification.CreatePushDataUseCase
import com.khrd.pingapp.pushnotification.CreatePushFailure
import com.khrd.pingapp.pushnotification.CreatePushSuccess
import com.khrd.pingapp.repository.pings.PingsRepository
import com.khrd.pingapp.utils.PingConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreatePingUseCaseImpl @Inject constructor(
    private val pingsRepository: PingsRepository,
    private val firebaseAuthAPI: FirebaseAuthAPI,
    private val connectionStatus: ConnectionStatus,
    private val createPushDataUseCase: CreatePushDataUseCase,
    private val sendPushUseCase: SendPushUseCase,
    @MainCoroutineScope private val mainCoroutineScope: CoroutineScope
) : CreatePingUseCase {
    override fun createPing(ping: PingData, callback: (CreatePingState) -> Unit) {
        firebaseAuthAPI.currentUserId()?.let { from ->
            ping.from = from
            processCreatingPing(ping, callback)
        } ?: callback(CreatePingFailure())
    }

    private fun processCreatingPing(ping: PingData, callback: (CreatePingState) -> Unit) {
        if (connectionStatus.getConnectionStatus()) {
            sendPing(ping, callback)
        } else {
            callback(CreatePingOfflineState)
            cachePingToDb(ping)
        }
    }

    private fun cachePingToDb(ping: PingData) {
        val offlinePing = PingConverter().convertDatabasePing(ping)
        if (ping.scheduledTime == null) {
            pingsRepository.cacheSentPingOffline(offlinePing)
        }
    }

    private fun sendPing(ping: PingData, callback: (CreatePingState) -> Unit) {
        pingsRepository.createPing(ping) { state ->
            when (state) {
                is PingStateSuccess -> {
                    callback(CreatePingSuccess(state.pings))
                    if (state.pings[0].scheduledTime == null) {
                        sendPush(state.pings[0])
                    }
                }
                is PingStateFailure -> {
                    callback(CreatePingFailure())
                }
                PingOfflineState -> callback(CreatePingOfflineState)
            }
        }
    }

    private fun sendPush(databasePing: DatabasePing) {
        createPushDataUseCase.createPushData(databasePing) {
            when (it) {
                is CreatePushSuccess -> {
                    mainCoroutineScope.launch {
                        sendPushUseCase.sendPush(it.pushNotification)
                    }
                }
                is CreatePushFailure -> {
                    Log.d("*****", "Push creating failed")
                }
            }
        }
    }

}