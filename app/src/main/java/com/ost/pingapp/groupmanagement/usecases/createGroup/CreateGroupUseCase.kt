package com.khrd.pingapp.groupmanagement.usecases.createGroup

import com.khrd.pingapp.data.groups.GroupState

interface CreateGroupUseCase {
    fun createGroup(userId: String, name: String?, callback: (GroupState) -> Unit)
}