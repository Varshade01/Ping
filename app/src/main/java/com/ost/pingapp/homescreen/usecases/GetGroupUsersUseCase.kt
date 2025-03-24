package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.homescreen.states.GetGroupUsersState

interface GetGroupUsersUseCase {
    fun getGroupUsers(groupId: String, loadFlag: DataLoadFlag, callback: (GetGroupUsersState) -> Unit)
}