package com.khrd.pingapp.groupmanagement.usecases.joinGroup

import com.khrd.pingapp.data.groups.GroupError
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.repository.groups.GroupRepository
import javax.inject.Inject

class GetGroupByLinkUseCaseImpl @Inject constructor(
    val groupRepository: GroupRepository,
    val firebaseConnectionStatus: ConnectionStatus
): GetGroupByLinkUseCase {
    override fun getGroupByLink(link: String, callback: (GroupState) -> Unit) {
        if(firebaseConnectionStatus.getConnectionStatus()) {
            groupRepository.getGroupByLink(link) {
                callback(it)
            }
        } else {
            callback(GroupFailure(GroupError.NETWORK_ERROR))
        }
    }
}