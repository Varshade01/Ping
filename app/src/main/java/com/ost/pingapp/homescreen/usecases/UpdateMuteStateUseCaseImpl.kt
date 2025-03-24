package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.repository.users.UsersRepository

class UpdateMuteStateUseCaseImpl(private val usersRepository: UsersRepository) : UpdateMuteStateUseCase {
    override fun muteItem(currentUserId: String, itemId: String, callback: ((UserRequestState) -> Unit)?) {
        usersRepository.muteItem(currentUserId, itemId, callback)
    }

    override fun unMuteItem(currentUserId: String, itemId: String, callback: ((UserRequestState) -> Unit)?) {
        usersRepository.unMuteItem(currentUserId, itemId, callback)
    }
}