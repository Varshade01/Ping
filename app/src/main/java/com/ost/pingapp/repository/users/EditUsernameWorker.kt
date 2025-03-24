package com.khrd.pingapp.repository.users

import android.content.Context
import android.os.ConditionVariable
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.states.RenameUsernameOfflineState
import com.khrd.pingapp.homescreen.states.RenameUsernameStateFailure
import com.khrd.pingapp.homescreen.states.RenameUsernameStateSuccess
import com.khrd.pingapp.homescreen.usecases.username.RenameUserNameUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EditUsernameWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    val updateUserNameUseCase: RenameUserNameUseCase,
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
        val offlineUsername = workerParameters.inputData.getString(KEY_OFFLINE_USERNAME)

        return if (offlineUsername != null) {
            updateUsername(offlineUsername)
        } else Result.failure()
    }

    private fun updateUsername(username: String): Result {
        var result = Result.failure()
        updateUserNameUseCase.renameUsername(username) {
            result = when (it) {
                is RenameUsernameStateSuccess -> Result.success()
                is RenameUsernameOfflineState -> Result.retry()
                is RenameUsernameStateFailure -> Result.failure()

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
        const val KEY_OFFLINE_USERNAME = "offline_username"
    }
}