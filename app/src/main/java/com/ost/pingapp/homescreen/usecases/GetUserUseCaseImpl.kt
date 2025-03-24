package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.homescreen.states.GetUserFailure
import com.khrd.pingapp.homescreen.states.GetUserState
import com.khrd.pingapp.homescreen.states.GetUserSuccess
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class GetUserUseCaseImpl @Inject constructor(
    val usersRepository: UsersRepository
) : GetUserUseCase {
    override fun getUser(userId: String?, loadFlag: DataLoadFlag, callback: (GetUserState) -> Unit) {
        if (userId != null) {
            usersRepository.getUser(userId, loadFlag) {
                when (it) {
                    is UserRequestState.UserRequestSuccess -> {
                        callback(
                            GetUserSuccess(
                                it.user?.copy() ?: DatabaseUser(id = userId)
                            )
                        )
                    }
                    UserRequestState.UserRequestFail -> {
                        callback(GetUserFailure)
                    }
                }
            }
        }
    }
}
