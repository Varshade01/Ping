package com.khrd.pingapp.workmanager

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.google.gson.Gson
import com.khrd.pingapp.alarmManager.ReschedulePingsWorker
import com.khrd.pingapp.constants.*
import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.firebase.HandleNewReceivedPingWorker
import com.khrd.pingapp.repository.groups.EditGroupNameOfflineWorker
import com.khrd.pingapp.repository.groups.UpdateGroupImageOfflineWorker
import com.khrd.pingapp.repository.pings.CancelScheduledPingOfflineWorker
import com.khrd.pingapp.repository.pings.SendPingOfflineWorker
import com.khrd.pingapp.repository.users.EditJobPositionWorker
import com.khrd.pingapp.repository.users.EditUsernameWorker
import javax.inject.Inject

class PingAppWorkManagerImpl @Inject constructor(private val context: Context) : PingAppWorkManager {

    private var workManager = WorkManager.getInstance(context)

    override fun startReschedulePingsWorker(recreateNewAlarms: Boolean) {
        val recreateAlarmArg = workDataOf(ReschedulePingsWorker.RECREATE_NEW_ALARMS to recreateNewAlarms)
        startWorker<ReschedulePingsWorker>(recreateAlarmArg)
    }

    override fun startEditUserNameWorker(userName: String) {
        startWorker<EditUsernameWorker>(workDataOf(EditUsernameWorker.KEY_OFFLINE_USERNAME to userName))
    }

    override fun startEditJobPositionWorker(jobPosition: String) {
        startWorker<EditJobPositionWorker>(workDataOf(EditJobPositionWorker.KEY_OFFLINE_JOB_POSITION to jobPosition))
    }

    override fun startCancelScheduledPingOfflineWorker(pingId: String) {
        startWorker<CancelScheduledPingOfflineWorker>(workDataOf(CancelScheduledPingOfflineWorker.CANCEL_SCHEDULED_PING_ID to pingId))
    }

    override fun startSendPingOfflineWorker(pingData: PingData) {
        val offlinePing = convertPingDataToJson(pingData)
        startWorker<SendPingOfflineWorker>(workDataOf(SendPingOfflineWorker.OFFLINE_PING to offlinePing))
    }

    override fun startHandleNewReceivedPingWorker(pingId: String) {
        startWorker<HandleNewReceivedPingWorker>(workDataOf(PushConstants.PING_ID to pingId))
    }

    override fun startUpdateGroupImageOfflineWorker(groupId: String, uri: Uri) {
        startWorker<UpdateGroupImageOfflineWorker>(
            workDataOf(
                UpdateGroupImageOfflineWorker.KEY_OFFLINE_GROUP_ID to groupId,
                UpdateGroupImageOfflineWorker.KEY_OFFLINE_IMAGE_URI to uri.toString(),
            )
        )
    }

    override fun startUpdateGroupNameOfflineWorker(groupId: String, name: String) {
        startWorker<EditGroupNameOfflineWorker>(
            workDataOf(
                EditGroupNameOfflineWorker.KEY_OFFLINE_GROUP_ID to groupId,
                EditGroupNameOfflineWorker.KEY_OFFLINE_GROUP_NAME to name,
            )
        )
    }


    private inline fun <reified W : ListenableWorker> startWorker(inputData: Data) {
        val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<W>()
            .setConstraints(networkConstraint)
            .setInputData(inputData)
            .build()

        workManager.enqueue(request)
    }

    private fun convertPingDataToJson(pingData: PingData): String {
        val gson = Gson()
        return gson.toJson(pingData)
    }

}