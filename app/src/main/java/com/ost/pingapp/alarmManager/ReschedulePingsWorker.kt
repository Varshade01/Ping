package com.khrd.pingapp.alarmManager

import android.content.Context
import android.os.ConditionVariable
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases.GetScheduledPingsUseCase
import com.khrd.pingapp.homescreen.sendping.SendPushUseCase
import com.khrd.pingapp.homescreen.states.RescheduleExpiredRecurringPingsStateFailure
import com.khrd.pingapp.homescreen.states.RescheduleExpiredRecurringPingsStateSuccess
import com.khrd.pingapp.homescreen.states.RewriteScheduledPingsFailure
import com.khrd.pingapp.homescreen.states.RewriteScheduledPingsSuccess
import com.khrd.pingapp.homescreen.usecases.pings.RescheduleExpiredRecurringPingsUseCase
import com.khrd.pingapp.homescreen.usecases.pings.RewriteScheduledPingsUseCase
import com.khrd.pingapp.pushnotification.CreatePushDataUseCase
import com.khrd.pingapp.pushnotification.NotificationUtils
import com.khrd.pingapp.utils.PingsAlarmManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
class ReschedulePingsWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    val notificationUtils: NotificationUtils,
    val connectionStatus: ConnectionStatus,
) : Worker(appContext, workerParams) {

    @Inject
    lateinit var sendPushUseCase: SendPushUseCase

    @Inject
    lateinit var createPushDataUseCase: CreatePushDataUseCase

    @Inject
    lateinit var scheduledPingsUseCase: GetScheduledPingsUseCase

    @Inject
    lateinit var rewriteScheduledPingsUseCase: RewriteScheduledPingsUseCase

    @Inject
    lateinit var rescheduleExpiredRecurringPingsUseCase: RescheduleExpiredRecurringPingsUseCase

    private var recreateNewAlarms: Boolean = false

    @Inject
    lateinit var pingsAlarmManager: PingsAlarmManager

    @Inject
    @IoCoroutineScope
    lateinit var coroutineScope: CoroutineScope

    override fun doWork(): Result {
        return processWork()
    }

    private fun processWork(): Result {
        val conditionVariable = ConditionVariable()
        recreateNewAlarms = workerParams.inputData.getBoolean(RECREATE_NEW_ALARMS, false)

        setForegroundAsync(notificationUtils.createSyncingPingsForegroundInfo())

        val job = coroutineScope.launch {
            scheduledPingsUseCase.getScheduledPings(DataLoadFlag.LOAD_FROM_SERVER).collect { pings ->
                if (pings != null) {
                    while (!connectionStatus.getConnectionStatus()){
                        delay(100)
                    }
                    if (pings.isNotEmpty()) {
                        sendScheduledPings(pings)
                    }
                    conditionVariable.open()
                }
            }
        }
        conditionVariable.block(60000)
        job.cancel()
        return if (connectionStatus.getConnectionStatus()) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    private fun sendScheduledPings(pings: List<DatabasePing>) {
        sendExpiredScheduledPings(pings)
        rescheduleExpiredScheduledRecurringPings(pings)
        if (recreateNewAlarms) {
            setAlarmsForScheduledPings(pings, appContext)
        }
    }

    private fun setAlarmsForScheduledPings(pings: List<DatabasePing>, context: Context?) {
        val scheduleTime: List<Pair<String, Long>> =
            pings.filter { it.scheduledTime!! > System.currentTimeMillis() }
                .map { it.id to it.scheduledTime!! }
        scheduleTime.forEach {
            pingsAlarmManager.setAlarm(context, it.second, it.first)
        }
    }

    private fun sendExpiredScheduledPings(pings: List<DatabasePing>) {
        val doneSignal = CountDownLatch(pings.size)
        rewriteScheduledPingsUseCase.rewriteScheduledPings(pings) { afterRebootState ->
            when (afterRebootState) {
                is RewriteScheduledPingsFailure -> {
                    Log.d("*****", "Scheduled ping sending success")
                }
                is RewriteScheduledPingsSuccess -> {
                    Log.d("*****", "Scheduled ping sending failed")
                }
            }
            doneSignal.countDown()
        }
        doneSignal.await(1, TimeUnit.MINUTES)
    }

    private fun rescheduleExpiredScheduledRecurringPings(pings: List<DatabasePing>) {
        rescheduleExpiredRecurringPingsUseCase.rescheduleExpiredRecurringPings(pings) { sentPingState ->
            when (sentPingState) {
                is RescheduleExpiredRecurringPingsStateSuccess -> {
                    Log.d("*****", "Scheduled recurring ping sending success")
                    pingsAlarmManager.setAlarm(
                        appContext,
                        sentPingState.databasePing.scheduledTime!!,
                        sentPingState.databasePing.id
                    )
                }
                is RescheduleExpiredRecurringPingsStateFailure -> {
                    Log.d("*****", "Scheduled recurring ping sending failure")
                }
            }
        }
    }

    companion object {
        const val RECREATE_NEW_ALARMS = "RECREATE_NEW_ALARMS"
    }
}