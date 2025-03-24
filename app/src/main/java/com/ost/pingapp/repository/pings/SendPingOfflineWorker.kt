package com.khrd.pingapp.repository.pings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.ConditionVariable
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.khrd.pingapp.alarmManager.ScheduledPingsReceiver
import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.states.CreatePingFailure
import com.khrd.pingapp.homescreen.states.CreatePingOfflineState
import com.khrd.pingapp.homescreen.states.CreatePingSuccess
import com.khrd.pingapp.homescreen.usecases.pings.CreatePingUseCase
import com.khrd.pingapp.pushnotification.NotificationUtils
import com.khrd.pingapp.utils.PingsAlarmManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class SendPingOfflineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val createPingUseCase: CreatePingUseCase,
    val connectionStatus: ConnectionStatus,
    val notificationUtils: NotificationUtils
    ) : Worker(context, workerParameters) {
    private val workerLock: ConditionVariable = ConditionVariable()

    @Inject
    lateinit var pingsAlarmManager: PingsAlarmManager

    override fun doWork(): Result {
        return if (connectionStatus.retrieveConnectionStatus()) {
            processWork()
        } else {
            Result.retry()
        }
    }

    /*
    * Receive input data from worker parameters,
    * convert it to pingData,s
    * create ping
    */
    private fun processWork(): Result {
        setForegroundAsync(notificationUtils.createSyncingPingsForegroundInfo())
        val offlinePingJson = workerParameters.inputData.getString(OFFLINE_PING)
        val pingData = offlinePingJson?.let { convertJsonToPingData(it) }
        return createPing(pingData)
    }

    /*
    * Create ping and returns result, depending on the CreatePingState
    */
    private fun createPing(
        pingData: PingData?,
    ): Result {
        var result = Result.failure()
        pingData?.let { data ->
            createPingUseCase.createPing(data) {
                result = when (it) {
                    is CreatePingFailure -> Result.failure()
                    is CreatePingOfflineState -> Result.retry()
                    is CreatePingSuccess -> {
                        val newPing = it.pings[0]
                        if (newPing.scheduledTime != null) {
                            pingsAlarmManager.setAlarm(applicationContext, newPing.scheduledTime, newPing.id,)
                        }
                        Result.success()
                    }
                }
                workerLock.open()
            }
        } ?: return result
        //reset previous lock state
        workerLock.close()
        //block worker
        workerLock.block()
        return result
    }

    /*
    * Converts json string from worker parameters to PingData class
    */
    private fun convertJsonToPingData(inputJson: String): PingData {
        val gson = Gson()
        return gson.fromJson(inputJson, PingData::class.java)
    }

    companion object{
        const val OFFLINE_PING = "OFFLINE_PING"
    }
}