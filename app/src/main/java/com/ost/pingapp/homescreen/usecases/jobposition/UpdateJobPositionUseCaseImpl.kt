package com.khrd.pingapp.homescreen.usecases.jobposition

import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class UpdateJobPositionUseCaseImpl @Inject constructor(
    private val userRepository: UsersRepository,
    private val connectionStatus: ConnectionStatus,
    private val firebaseAuth: FirebaseAuthAPI
) : UpdateJobPositionUseCase {
    override fun updateJobPosition(job: String, callback: (UserRequestState) -> Unit) {
        firebaseAuth.currentUserId()?.let {
            if (connectionStatus.getConnectionStatus()) {
                userRepository.updateUserJob(it, job) { callback(it) }
            } else {
                callback(UserRequestState.UserRequestOffline(DatabaseUser(id = it, job = job)))
                userRepository.updateUserJobInCache(id = it,job = job)
            }
        } ?: callback(UserRequestState.UserRequestFail)
    }
}