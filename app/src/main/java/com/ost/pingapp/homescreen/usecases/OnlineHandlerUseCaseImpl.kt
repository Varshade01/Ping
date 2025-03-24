package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.data.users.Online
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class OnlineHandlerUseCaseImpl @Inject constructor(
    private val usersRepository: UsersRepository,
    private val firebaseAuth: FirebaseAuthAPI
) : OnlineHandlerUseCase {
    override fun sendOnlineStatus(status: Boolean, currentUserId: String?) {
        val userId = currentUserId ?: firebaseAuth.currentUserId()
        userId?.let {
            usersRepository.updateUserOnlineStatus(it, Online(status, System.currentTimeMillis()))
        }
    }
}