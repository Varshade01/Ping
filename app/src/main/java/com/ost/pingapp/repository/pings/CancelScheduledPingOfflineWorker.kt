package com.khrd.pingapp.repository.pings

import android.content.Context
import android.os.ConditionVariable
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.states.*
import com.khrd.pingapp.homescreen.usecases.pings.DeleteScheduledPingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CancelScheduledPingOfflineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val deleteScheduledPingsUseCase: DeleteScheduledPingsUseCase,
    val connectionStatus: ConnectionStatus
) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        return if (connectionStatus.retrieveConnectionStatus()) {
            processWork()
        } else {
            Result.retry()
        }
    }

    private fun processWork(): Result {
        val scheduledPingId = workerParameters.inputData.getString(CANCEL_SCHEDULED_PING_ID)
        return deleteScheduledPing(scheduledPingId)
    }

    private fun deleteScheduledPing(scheduledPingId: String?): Result {
       val workerLock: ConditionVariable = ConditionVariable()
        var result = Result.failure()
        scheduledPingId?.let {
            deleteScheduledPingsUseCase.deleteScheduledPings(it) {
                result = when (it) {
                    is DeleteScheduledPingFailure -> Result.failure()
                    is DeleteScheduledPingOffline -> Result.retry()
                    is DeleteScheduledPingSuccess -> Result.success()
                }
                workerLock.open()
            }
        } ?: return result
        //block worker
        workerLock.block()
        return result
    }

    companion object{
        const val CANCEL_SCHEDULED_PING_ID = "CANCEL_SCHEDULED_PING_ID"
    }

}