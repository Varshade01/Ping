package com.khrd.pingapp.homescreen.usecases

import android.os.ConditionVariable
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.homescreen.listeners.UsersListener
import com.khrd.pingapp.homescreen.states.GetGroupUsersFailure
import com.khrd.pingapp.homescreen.states.GetGroupUsersState
import com.khrd.pingapp.homescreen.states.GetGroupUsersSuccess
import com.khrd.pingapp.homescreen.sendping.GetGroupMembersUseCase
import com.khrd.pingapp.homescreen.sendping.GetUserGroupsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class GetGroupUsersUseCaseImpl @Inject constructor(
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val getUserGroupsUseCase: GetUserGroupsUseCase,
    private val getGroupUseCase: GetGroupUseCase,
    @IoCoroutineScope private val ioCoroutineScope: CoroutineScope
) : GetGroupUsersUseCase {
    override fun getGroupUsers(groupId: String, loadFlag: DataLoadFlag, callback: (GetGroupUsersState) -> Unit) {
        getUserGroupsUseCase.getUserGroups(loadFlag) { userRequestState ->
            when (userRequestState) {
                UserRequestState.UserRequestFail -> callback(GetGroupUsersFailure)
                is UserRequestState.UserRequestSuccess -> {
                    if (!userRequestState.user?.groups.isNullOrEmpty()) {
                        getGroupUseCase.getGroup(groupId, loadFlag) { groupState ->
                            when (groupState) {
                                is GroupFailure -> callback(GetGroupUsersFailure)
                                is GroupSuccess -> {
                                    ioCoroutineScope.launch {
                                        val group = groupState.group
                                        val conditionVariable = ConditionVariable()
                                        getGroupMembersUseCase.addListener(groupId, object : UsersListener() {
                                            override fun onUsersChanged(groupId: String, users: List<DatabaseUser>?) {
                                                users?.let {
                                                    callback(GetGroupUsersSuccess(group, users))
                                                } ?: callback(GetGroupUsersFailure)
                                                getGroupMembersUseCase.removeListener(this)
                                                conditionVariable.open()
                                            }
                                        })
                                        getGroupMembersUseCase.getGroupMembers(groupId, loadFlag)
                                        conditionVariable.block()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}