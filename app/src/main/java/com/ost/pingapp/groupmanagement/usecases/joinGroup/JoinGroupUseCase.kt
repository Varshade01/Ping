package com.khrd.pingapp.groupmanagement.usecases.joinGroup

import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.data.pings.DataLoadFlag

interface JoinGroupUseCase {
    fun joinGroup(userId: String, groupId: String, loadFlag: DataLoadFlag, callback: (GroupState) -> Unit)
}