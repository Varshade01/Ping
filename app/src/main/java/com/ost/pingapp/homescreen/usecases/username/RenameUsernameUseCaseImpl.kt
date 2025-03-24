package com.khrd.pingapp.homescreen.usecases.username

import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.states.RenameUsernameOfflineState
import com.khrd.pingapp.homescreen.states.RenameUsernameState
import com.khrd.pingapp.homescreen.states.RenameUsernameStateFailure
import com.khrd.pingapp.homescreen.states.RenameUsernameStateSuccess
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class RenameUsernameUseCaseImpl @Inject constructor(
    private val usersRepository: UsersRepository,
    private val connectionStatus: ConnectionStatus,
    private val firebaseAuth: FirebaseAuthAPI
) : RenameUserNameUseCase {
    override fun renameUsername(newName: String, callback: (RenameUsernameState) -> Unit) {
        firebaseAuth.currentUserId()?.let {
            if (connectionStatus.getConnectionStatus()) {
                usersRepository.updateUsername(it, newName) { callback(RenameUsernameStateSuccess) }
            } else {
                callback(RenameUsernameOfflineState)
                usersRepository.updateUsernameCache(it, newName)
            }
        } ?: callback(RenameUsernameStateFailure)
    }
}
