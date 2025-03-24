package com.khrd.pingapp.repository.users

import android.content.Context
import android.os.ConditionVariable
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.usecases.jobposition.UpdateJobPositionUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EditJobPositionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    val updateJobPositionUseCase: UpdateJobPositionUseCase,
    val connectionStatus: ConnectionStatus
) : Worker(context, workerParameters) {

    private val workerLock: ConditionVariable = ConditionVariable()

    override fun doWork(): Result {
        return if (connectionStatus.retrieveConnectionStatus()) {
            processWork()
        } else {
            Result.retry()
        }
    }

    private fun processWork(): Result {
        val offlineJobPosition = workerParameters.inputData.getString(KEY_OFFLINE_JOB_POSITION)

        return if (offlineJobPosition != null) {
            updateJobPosition(offlineJobPosition)
        } else Result.failure()
    }

    private fun updateJobPosition(jobPosition: String): Result {
        var result = Result.failure()
        updateJobPositionUseCase.updateJobPosition(jobPosition) {
            result = when (it) {
                is UserRequestState.UserRequestSuccess -> Result.success()
                is UserRequestState.UserRequestOffline -> Result.retry()
                is UserRequestState.UserRequestFail -> Result.failure()
            }
            workerLock.open()
        }
        //reset previous lock state
        workerLock.close()
        //block worker
        workerLock.block()
        return result
    }

    companion object{
        const val KEY_OFFLINE_JOB_POSITION = "offline_jobPosition"
    }
}