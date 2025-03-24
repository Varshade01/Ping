package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class HideUserInformationUseCaseImpl @Inject constructor(
    private val usersRepository: UsersRepository,
) : HideUserInformationUseCase {

    override fun hideUserInfo(currentUserId: String, isHide: Boolean, callback: (UserRequestState) -> Unit) {
        usersRepository.hideUserInfo(currentUserId, isHide, callback)
    }
}