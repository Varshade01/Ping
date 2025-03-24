package com.khrd.pingapp.groupmanagement.usecases

import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.pings.DataLoadFlag

interface GetGroupsByIdsUseCase {
    fun getGroups(groupIds: List<String>, loadFlag: DataLoadFlag, callback: (List<DatabaseGroup>) -> Unit)
}