package com.khrd.pingapp.firebase

import android.content.Context
import android.os.ConditionVariable
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.khrd.pingapp.constants.PushConstants.PING_ID
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.CheckMutedItemsUseCase
import com.khrd.pingapp.homescreen.usecases.pings.received.LoadReceivedPingsUseCase
import com.khrd.pingapp.pushnotification.NotificationUtils
import com.khrd.pingapp.pushnotification.ShowPushNotificationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltWorker
class HandleNewReceivedPingWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IoCoroutineScope val ioCoroutineScope: CoroutineScope,
    private val connectionStatus: ConnectionStatus,
    private val firebaseAuthAPI: FirebaseAuthAPI,
    private val notificationUtils: NotificationUtils,
    private val loadReceivedPingsUseCase: LoadReceivedPingsUseCase,
    private val showPushNotificationUseCase: ShowPushNotificationUseCase,
    private val checkMutedItemsUseCase: CheckMutedItemsUseCase,
) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return if (connectionStatus.retrieveConnectionStatus()) {
            processWork()
        } else {
            Result.retry()
        }
    }

    private fun processWork(): Result {
        val pingId = inputData.getString(PING_ID)
        val conditionVariable = ConditionVariable()
        var receivedPings: List<DatabasePing> = listOf()
        var newPing: DatabasePing? = null
        val timeout: Long = 30000
        setForegroundAsync(notificationUtils.createSyncingPingsForegroundInfo())

        val job = ioCoroutineScope.launch {
            loadReceivedPingsUseCase.loadReceivedPings().collect { pingsData ->
                receivedPings = pingsData?.listOfPings?.filter { !it.views.containsKey(firebaseAuthAPI.currentUserId()) } ?: emptyList()
                newPing = receivedPings.find { it.id == pingId }
                if (newPing != null) {
                    conditionVariable.open()
                }
            }
        }
        conditionVariable.block(timeout)
        job.cancel()
        conditionVariable.close()

        handleNotification(newPing, receivedPings)
        return Result.success()
    }

    private fun handleNotification(newPing: DatabasePing?, receivedPings: List<DatabasePing>) {
        newPing?.let { databasePing ->
            if (!checkMutedItemsUseCase.containsMutedItems(databasePing)) {
                showPushNotificationUseCase.showPushNotification(databasePing, receivedPings.size, appContext)
            }
        }
    }
}