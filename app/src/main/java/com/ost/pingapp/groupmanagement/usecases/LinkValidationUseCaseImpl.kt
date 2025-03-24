package com.khrd.pingapp.groupmanagement.usecases

import com.khrd.pingapp.data.groups.GroupError
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.repository.groups.GroupRepository
import javax.inject.Inject

class LinkValidationUseCaseImpl @Inject constructor(
    private val groupRepository: GroupRepository,
    private val firebaseConnectionStatus: ConnectionStatus
) : LinkValidationUseCase {
    override fun validateLink(link: String, callback: (GroupState) -> Unit) {
        if (firebaseConnectionStatus.getConnectionStatus()) {
            groupRepository.getGroupByLink(link) {
                when (it) {
                    is GroupFailure -> {
                        callback(it)
                    }
                    is GroupSuccess -> callback(it)
                }
            }
        } else {
            callback(GroupFailure(GroupError.NETWORK_ERROR))
        }
    }
}