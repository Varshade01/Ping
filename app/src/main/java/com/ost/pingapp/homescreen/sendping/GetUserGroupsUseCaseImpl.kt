package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class GetUserGroupsUseCaseImpl @Inject constructor(
    val firebaseAuth: FirebaseAuthAPI,
    val usersRepository: UsersRepository
): GetUserGroupsUseCase {
    override fun getUserGroups(loadFlag: DataLoadFlag, callback: (UserRequestState) -> Unit) {
        val userId = firebaseAuth.currentUserId()
        userId?.let {
            usersRepository.getUserGroups(it,loadFlag) {
                callback(it)
            }
        }
    }
}