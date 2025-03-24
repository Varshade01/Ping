package com.khrd.pingapp.repository.groups

import android.content.Context
import android.os.ConditionVariable
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupOfflineState
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.RenameGroupUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EditGroupNameOfflineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val renameGroupUseCase: RenameGroupUseCase,
    private val connectionStatus: ConnectionStatus,
) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        return if (connectionStatus.retrieveConnectionStatus()) {
            processWork()
        } else {
            Result.retry()
        }
    }

    private fun processWork(): Result {
        val groupId = workerParameters.inputData.getString(KEY_OFFLINE_GROUP_ID)
        val offlineGroupName = workerParameters.inputData.getString(KEY_OFFLINE_GROUP_NAME)

        return if (groupId != null && offlineGroupName != null) {
            updateGroupName(groupId, offlineGroupName)
        } else Result.failure()
    }

    private fun updateGroupName(groupId: String, groupName: String): Result {
        val workerLock = ConditionVariable()

        var result = Result.failure()
        renameGroupUseCase.renameGroup(groupId, groupName) { groupState ->
            result = when (groupState) {
                is GroupSuccess -> Result.success()
                is GroupOfflineState -> Result.retry()
                is GroupFailure -> Result.failure()
                else -> Result.failure()
            }
            workerLock.open()
        }
        //reset previous lock state
        workerLock.close()
        //block worker
        workerLock.block()
        return result
    }

    companion object {
        const val KEY_OFFLINE_GROUP_ID = "offline_group_id_key"
        const val KEY_OFFLINE_GROUP_NAME = "offline_group_name_key"
    }
}