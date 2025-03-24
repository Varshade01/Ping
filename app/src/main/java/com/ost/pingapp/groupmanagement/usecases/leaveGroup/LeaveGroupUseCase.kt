package com.khrd.pingapp.groupmanagement.usecases.leaveGroup

import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.data.pings.DataLoadFlag

interface LeaveGroupUseCase {
    fun leaveGroup(groupId: String, callback: (GroupState) -> Unit)
}