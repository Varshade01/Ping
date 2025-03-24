package com.khrd.pingapp.groupmanagement.usecases

import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.data.pings.DataLoadFlag

interface GetGroupUseCase {
    fun getGroup(groupId: String?, loadFlag: DataLoadFlag, callback: (GroupState) -> Unit)
    suspend fun getGroupSuspend(groupId: String?, loadFlag: DataLoadFlag): GroupState
}