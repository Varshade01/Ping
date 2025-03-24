package com.khrd.pingapp.groupmanagement.usecases.createGroup

import com.khrd.pingapp.data.groups.*
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.repository.groups.GroupRepository
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class CreateGroupUseCaseImpl @Inject constructor(
    private val groupRepository: GroupRepository,
    private val usersRepository: UsersRepository,
    private val firebaseConnectionStatus: ConnectionStatus
) : CreateGroupUseCase {

    override fun createGroup(userId: String, name: String?, callback: (GroupState) -> Unit) {
        if(firebaseConnectionStatus.getConnectionStatus()) {
            groupRepository.createGroup(userId, name) {
                when (it) {
                    is GroupFailure -> callback(it)
                    is GroupSuccess -> {
                        addUserGroup(userId, it.group.id) {
                            callback(it)
                        }
                    }
                    is GroupOfflineState -> {

                    }
                }
            }
        } else {
            callback(GroupFailure(GroupError.NETWORK_ERROR))
        }
    }

    private fun addUserGroup(userId: String, groupId: String, callback: (GroupState) -> Unit) {
        usersRepository.addUserGroup(userId, groupId) {
            when (it) {
                UserRequestState.UserRequestFail -> callback(GroupFailure(GroupError.UNKNOWN_ERROR))
                is UserRequestState.UserRequestOffline -> callback(GroupFailure(GroupError.NETWORK_ERROR))
                is UserRequestState.UserRequestSuccess -> callback(GroupSuccess(DatabaseGroup(id = groupId)))
            }
        }
    }
}