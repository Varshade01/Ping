package com.khrd.pingapp.groupmanagement.usecases

import com.khrd.pingapp.data.groups.GroupError
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.repository.groups.GroupRepository
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetGroupUseCaseImpl @Inject constructor(
    private val groupsRepository: GroupRepository
) : GetGroupUseCase {
    override fun getGroup(groupId: String?, loadFlag: DataLoadFlag, callback: (GroupState) -> Unit) {
        if (groupId != null) {
            groupsRepository.getGroup(groupId, loadFlag) { callback(it) }
        } else {
            callback(GroupFailure(GroupError.UNKNOWN_ERROR))
        }
    }

    override suspend fun getGroupSuspend(groupId: String?, loadFlag: DataLoadFlag): GroupState {
        return suspendCoroutine { continuation ->
            getGroup(groupId, loadFlag) {
                continuation.resume(it)
            }
        }
    }
}