package com.khrd.pingapp.groupmanagement.usecases.renameGroup

import com.khrd.pingapp.data.groups.GroupState

interface RenameGroupUseCase {
    fun renameGroup(groupId: String, name: String, callback: (GroupState) -> Unit)
}