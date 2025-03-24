package com.khrd.pingapp.homescreen.usecases.pings

import android.util.Log
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.data.pings.RecurringTime
import com.khrd.pingapp.homescreen.states.*
import com.khrd.pingapp.utils.PingsAlarmManager
import java.util.*
import javax.inject.Inject

class RescheduleExpiredRecurringPingsUseCaseImpl @Inject constructor(
    val createPingUseCase: CreatePingUseCase,
    val deleteScheduledPingsUseCase: DeleteScheduledPingsUseCase
) : RescheduleExpiredRecurringPingsUseCase {

    @Inject
    lateinit var pingsAlarmManager: PingsAlarmManager

    override fun rescheduleExpiredRecurringPings(pings: List<DatabasePing>, callback: (RescheduleExpiredRecurringPingsState) -> Unit) {
        //get expired unsent pings that are recurring
        val overduePings = pings.filter { ping -> ping.scheduledTime!! <= System.currentTimeMillis() && ping.recurringTime != RecurringTime.NO_REPEAT }
            .map {
                PingData(
                    pingId = it.id,
                    from = it.from.keys.toList()[0],
                    receivers = it.receivers.keys.toList(),
                    emoji = it.message,
                    groupId = it.groupId,
                    timestamp = it.scheduledTime,
                    groupFrom = it.groupFrom,
                    recurringTime = it.recurringTime,
                    scheduledTime = Date(it.scheduledTime!!)
                )
            }

        overduePings.forEach { scheduled ->
            //get and create last unsent expired ping
            val latestExpiredPing = scheduled.copy(scheduledTime = getLatestExpiredTime(scheduled.scheduledTime!!, scheduled.recurringTime))
            createPing(latestExpiredPing)

            //get and create next scheduled ping
            val newScheduledPing =
                scheduled.copy(
                    timestamp = latestExpiredPing.scheduledTime!!.time,
                    scheduledTime = getNewScheduleTime(latestExpiredPing.scheduledTime!!, latestExpiredPing.recurringTime)
                )
            createScheduledPing(newScheduledPing, callback)
        }
    }

    private fun getLatestExpiredTime(scheduledTime: Date, recurringTime: RecurringTime): Date {
        val calendar = Calendar.getInstance()
        var lastExpiredPingScheduledTime: Date
        calendar.timeInMillis = scheduledTime.time
        val currTime = System.currentTimeMillis()
        //searching for the scheduled time of the last unsent expired ping
        do {
            lastExpiredPingScheduledTime = calendar.time
            addRecurringTime(recurringTime, calendar)
        }
        while (calendar.time.time < currTime)

        return lastExpiredPingScheduledTime
    }

    private fun getNewScheduleTime(scheduledTime: Date, recurringTime: RecurringTime): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = scheduledTime.time

        addRecurringTime(recurringTime, calendar)

        return calendar.time
    }

    private fun addRecurringTime(recurringTime: RecurringTime, calendar: Calendar) {
        if (recurringTime == RecurringTime.DAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        if (recurringTime == RecurringTime.WEEK) {
            calendar.add(Calendar.WEEK_OF_MONTH, 1)
        }
        if (recurringTime == RecurringTime.MONTH) {
            calendar.add(Calendar.MONTH, 1)
        }
        if (recurringTime == RecurringTime.YEAR) {
            calendar.add(Calendar.YEAR, 1)
        }
    }

    private fun createPing(ping: PingData) {
        val newPing = ping.copy(timestamp = ping.scheduledTime!!.time, scheduledTime = null)
        createPingUseCase.createPing(newPing) { createState ->
            when (createState) {
                is CreatePingSuccess -> {
                    Log.d("*****", "Ping sending success")
                }
                is CreatePingFailure -> {
                    Log.d("*****", "Ping sending failure")
                }
                CreatePingOfflineState -> Log.d("*****", "Ping sending failure")
            }
        }
    }

    private fun createScheduledPing(scheduledPing: PingData, callback: (RescheduleExpiredRecurringPingsState) -> Unit) {
        createPingUseCase.createPing(scheduledPing) { createState ->
            when (createState) {
                is CreatePingSuccess -> {
                    deleteScheduledPingsUseCase.deleteScheduledPings(scheduledPing.pingId!!) { deleteState ->
                        when (deleteState) {
                            is DeleteScheduledPingSuccess -> {
                                callback(RescheduleExpiredRecurringPingsStateSuccess(createState.pings[0]))
                            }
                            is DeleteScheduledPingFailure -> {
                                callback(RescheduleExpiredRecurringPingsStateFailure())
                            }
                            else -> RescheduleExpiredRecurringPingsStateFailure()
                        }
                    }

                }
                is CreatePingFailure -> {
                    callback(RescheduleExpiredRecurringPingsStateFailure())
                }
                CreatePingOfflineState -> RescheduleExpiredRecurringPingsStateFailure()
            }
        }
    }
}