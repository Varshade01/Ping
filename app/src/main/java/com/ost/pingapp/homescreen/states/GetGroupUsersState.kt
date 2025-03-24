package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.users.DatabaseUser

sealed interface GetGroupUsersState

data class GetGroupUsersSuccess(val group: DatabaseGroup, val members: List<DatabaseUser>): GetGroupUsersState

object GetGroupUsersFailure: GetGroupUsersState