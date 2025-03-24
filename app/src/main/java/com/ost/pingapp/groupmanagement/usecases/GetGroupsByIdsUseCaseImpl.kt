package com.khrd.pingapp.groupmanagement.usecases

import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.repository.groups.GroupRepository

class GetGroupsByIdsUseCaseImpl(
    val groupRepository: GroupRepository
): GetGroupsByIdsUseCase{
    override fun getGroups(groupIds: List<String>, loadFlag: DataLoadFlag, callback: (List<DatabaseGroup>) -> Unit) {
        groupRepository.getGroups(groupIds, loadFlag) {
            callback(it)
        }
    }
}