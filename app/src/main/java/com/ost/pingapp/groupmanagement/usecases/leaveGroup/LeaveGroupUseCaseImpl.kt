package com.khrd.pingapp.groupmanagement.usecases.leaveGroup

import com.khrd.pingapp.data.groups.*
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.states.GetGroupUsersFailure
import com.khrd.pingapp.homescreen.states.GetGroupUsersSuccess
import com.khrd.pingapp.homescreen.usecases.GetGroupUsersUseCase
import com.khrd.pingapp.repository.groups.GroupRepository
import com.khrd.pingapp.repository.users.UsersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LeaveGroupUseCaseImpl @Inject constructor(
    private val usersRepository: UsersRepository,
    private val groupRepository: GroupRepository,
    private val getGroupUsersUseCase: GetGroupUsersUseCase,
    private val firebaseConnectionStatus: ConnectionStatus,
    private val firebaseAuth: FirebaseAuthAPI,
    private val dataStoreManager: DataStoreManager,
    @IoCoroutineScope private val coroutineScope: CoroutineScope
) : LeaveGroupUseCase {
    override fun leaveGroup(groupId: String, callback: (GroupState) -> Unit) {
        if (firebaseConnectionStatus.getConnectionStatus()) {
            getGroupUsersUseCase.getGroupUsers(groupId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { getGroupUsersState ->
                when (getGroupUsersState) {
                    GetGroupUsersFailure -> deleteUserGroup(groupId, callback, false)
                    is GetGroupUsersSuccess -> deleteUserGroup(groupId, callback, getGroupUsersState.members.size == 1)
                }
            }
        } else {
            callback(GroupFailure(GroupError.NETWORK_ERROR))
        }

    }

    private fun deleteUserGroup(
        groupId: String,
        callback: (GroupState) -> Unit,
        deleteGroup: Boolean
    ) {
        firebaseAuth.currentUserId()?.let { userId ->
            deleteUserGroup(userId, groupId) { groupState ->
                when (groupState) {
                    is GroupFailure -> callback(groupState);
                    is GroupOfflineState -> callback(GroupFailure(GroupError.NETWORK_ERROR))
                    is GroupSuccess -> {
                        coroutineScope.launch {
                            dataStoreManager.saveCurrentGroup("")
                            // If there was only one member of this group - delete this group on the server
                            if (deleteGroup) {
                                removeGroup(groupId) {
                                    callback(it)
                                }
                            } else {
                                callback(groupState)
                            }
                        }
                    }
                }
            }
        } ?: callback(GroupFailure(GroupError.UNKNOWN_ERROR))
    }

    private fun deleteUserGroup(userId: String, groupId: String, callback: (GroupState) -> Unit) {
        usersRepository.deleteUserGroup(userId, groupId) { userRequestState ->
            when (userRequestState) {
                UserRequestState.UserRequestFail ->
                    callback(GroupFailure(GroupError.UNKNOWN_ERROR))
                is UserRequestState.UserRequestSuccess -> {
                    callback(GroupSuccess(DatabaseGroup(id = groupId)))
                }
                is UserRequestState.UserRequestOffline -> {
                    callback(GroupOfflineState(DatabaseGroup(id = groupId)))
                }
            }
        }
    }

    private fun removeGroup(groupId: String, callback: (GroupState) -> Unit) {
        groupRepository.removeGroup(groupId) {
            callback(it)
        }
    }
}
