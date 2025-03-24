package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class SubscribeToUserChangesUseCaseImpl @Inject constructor(val usersRepository: UsersRepository) : SubscribeToUserChangesUseCase {

    override fun subscribeForUsersChanges(usersIds: List<String>, listener: () -> Unit) {
        usersRepository.subscribeForUsersChanges(usersIds, listener)
    }

    override fun unSubscribeForUsersChanges(listener: () -> Unit) {
        usersRepository.unSubscribeUsersChanges(listener)
    }
}