package com.khrd.pingapp.groupmanagement.usecases.renameGroup

import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.groups.GroupOfflineState
import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.repository.groups.GroupRepository
import javax.inject.Inject

class RenameGroupUseCaseImpl @Inject constructor(
    private val groupRepository: GroupRepository,
    private val firebaseConnectionStatus: ConnectionStatus
) : RenameGroupUseCase {
    override fun renameGroup(groupId: String, name: String, callback: (GroupState) -> Unit) {
        if (firebaseConnectionStatus.getConnectionStatus()) {
            groupRepository.updateGroupName(groupId, name) { callback(it) }
        } else {
            groupRepository.updateCachedGroupName(groupId, name)
            callback(GroupOfflineState(DatabaseGroup(id = groupId, name = name)))
        }
    }
}