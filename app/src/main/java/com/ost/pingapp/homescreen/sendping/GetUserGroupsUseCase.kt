package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.UserRequestState

interface GetUserGroupsUseCase {
    fun getUserGroups(loadFlag: DataLoadFlag, callback: (UserRequestState) -> Unit)
}