package com.khrd.pingapp.alarmManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.khrd.pingapp.workmanager.PingAppWorkManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//SchedulePingWorker
@AndroidEntryPoint
class ScheduledPingsReceiver : BroadcastReceiver() {
    @Inject
    lateinit var pingAppWorkManager: PingAppWorkManager

    override fun onReceive(context: Context?, intent: Intent?) {
        pingAppWorkManager.startReschedulePingsWorker(false)
    }
}