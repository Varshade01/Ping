package com.khrd.pingapp.firebase

import android.util.Log
import com.khrd.pingapp.utils.viewstate.AppViewState
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.khrd.pingapp.constants.PushConstants.PING_ID
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.homescreen.usecases.UpdateFcmTokenUseCase
import com.khrd.pingapp.workmanager.PingAppWorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PingFcmService : FirebaseMessagingService() {
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    @Inject
    lateinit var appViewState: AppViewState

    @Inject
    lateinit var pingAppWorkManager: PingAppWorkManager

    @Inject
    @IoCoroutineScope
    lateinit var coroutineScope: CoroutineScope

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
        val msgData = remoteMessage.data
        val pingId = msgData[PING_ID]
        if (!appViewState.isAppVisible) {
            pingId?.let { startHandlingNewReceivedPingWorker(it) }
        }
    }

    private fun startHandlingNewReceivedPingWorker(pingId: String) {
        pingAppWorkManager.startHandleNewReceivedPingWorker(pingId)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        coroutineScope.launch { dataStoreManager.saveFcmToken(token) }
        updateFcmTokenUseCase.updateFcmToken(token)

    }

    companion object {
        private const val TAG = "PingFcmService"
    }
}