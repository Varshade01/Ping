package com.khrd.pingapp.workmanager

import android.net.Uri
import com.khrd.pingapp.data.pings.PingData

interface PingAppWorkManager {
    fun startReschedulePingsWorker(recreateNewAlarms: Boolean)
    fun startEditUserNameWorker(userName: String)
    fun startEditJobPositionWorker(jobPosition: String)
    fun startCancelScheduledPingOfflineWorker(pingId: String)
    fun startSendPingOfflineWorker(pingData: PingData)
    fun startHandleNewReceivedPingWorker(pingId: String)
    fun startUpdateGroupImageOfflineWorker(groupId: String, uri: Uri)
    fun startUpdateGroupNameOfflineWorker(groupId: String, name: String)
}