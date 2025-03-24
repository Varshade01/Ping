package com.khrd.pingapp.useradministration.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class GetUsersByIdUseCaseImpl @Inject constructor(
    val usersRepository: UsersRepository
) : GetUsersByIdUseCase {
    override fun getUsers(userIds: List<String>, loadFlag: DataLoadFlag, callback: (List<DatabaseUser>) -> Unit) {
        usersRepository.getUsers(userIds, loadFlag) { state ->
            callback(state)
        }
    }
}