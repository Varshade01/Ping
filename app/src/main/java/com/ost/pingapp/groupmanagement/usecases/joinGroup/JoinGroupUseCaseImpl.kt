package com.khrd.pingapp.groupmanagement.usecases.joinGroup

import com.khrd.pingapp.data.groups.*
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.repository.groups.GroupRepository
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class JoinGroupUseCaseImpl @Inject constructor(
    val usersRepository: UsersRepository,
    val groupRepository: GroupRepository,
    val firebaseConnectionStatus: ConnectionStatus
) : JoinGroupUseCase {
    override fun joinGroup(userId: String, groupId: String, loadFlag: DataLoadFlag, callback: (GroupState) -> Unit) {
        if (firebaseConnectionStatus.getConnectionStatus()) {
            groupRepository.getGroup(groupId, loadFlag) {
                when (it) {
                    is GroupFailure -> callback(it)
                    is GroupSuccess -> {
                        val name = it.group.name
                        val link = it.group.invitationLink
                        usersRepository.addUserGroup(userId, groupId) { state ->
                            when (state) {
                                is UserRequestState.UserRequestFail -> callback(GroupFailure(GroupError.UNKNOWN_ERROR))
                                is UserRequestState.UserRequestSuccess -> callback(
                                    GroupSuccess(
                                        DatabaseGroup(
                                            id = groupId,
                                            name = name,
                                            invitationLink = link
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            callback(GroupFailure(GroupError.NETWORK_ERROR))
        }
    }
}