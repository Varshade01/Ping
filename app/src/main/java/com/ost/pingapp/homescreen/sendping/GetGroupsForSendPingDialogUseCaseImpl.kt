package com.khrd.pingapp.homescreen.sendping

import android.util.Log
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.homescreen.states.GetUserGroupsFailure
import com.khrd.pingapp.homescreen.states.GetUserGroupsState
import com.khrd.pingapp.homescreen.states.GetUserGroupsSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class GetGroupsForSendPingDialogUseCaseImpl @Inject constructor(
    private val getUserGroupsUseCase: GetUserGroupsUseCase,
    private val getGroupUseCase: GetGroupUseCase,
    @IoCoroutineScope private val ioCoroutineScope: CoroutineScope
) : GetGroupsForSendPingDialogUseCase {

    override fun loadGroups(callback: (GetUserGroupsState) -> Unit) {
        getUserGroupsUseCase.getUserGroups(DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) {
            when (it) {
                is UserRequestState.UserRequestSuccess -> {
                    loadListOfGroups(it.user?.groups?.keys?.toList()) { list ->
                        if (list.isNullOrEmpty()) {
                            callback(GetUserGroupsFailure)
                        } else {
                            callback(GetUserGroupsSuccess(list))
                        }
                    }
                }
                is UserRequestState.UserRequestFail -> {
                    callback(GetUserGroupsFailure)
                }
            }
        }
    }

    private fun loadListOfGroups(groupsIds: List<String>?, callback: (List<DatabaseGroup>) -> Unit) {
        groupsIds?.let {
            ioCoroutineScope.launch(Dispatchers.IO) {
                val listOfGroups = mutableListOf<DatabaseGroup>()
                val doneSignal = CountDownLatch(groupsIds.size)
                groupsIds.forEach { groupId ->
                    getGroupUseCase.getGroup(groupId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { state ->
                        when (state) {
                            is GroupSuccess -> listOfGroups.add(state.group)
                            is GroupFailure -> Log.e("*********", state.error.toString())
                        }
                        doneSignal.countDown()
                    }
                }
                doneSignal.await()
                callback(listOfGroups)
            }
        } ?: callback(emptyList())
    }
}