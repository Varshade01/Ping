package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.homescreen.listeners.UsersListener
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class GetGroupMembersUseCaseImpl @Inject constructor(
    private val usersRepository: UsersRepository
) : GetGroupMembersUseCase {

    override fun getGroupMembers(groupId: String, loadFlag: DataLoadFlag) {
        usersRepository.getGroupMembers(groupId, loadFlag)
    }

    override fun addListener(groupId: String, listener: UsersListener) {
        usersRepository.addListener(groupId, listener)
    }

    override fun removeListener(listener: UsersListener) {
        usersRepository.removeListener(listener)
    }

}