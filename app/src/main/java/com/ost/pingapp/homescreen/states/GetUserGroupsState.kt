package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.groups.DatabaseGroup

sealed interface GetUserGroupsState
    data class GetUserGroupsSuccess(val listOfGroups: List<DatabaseGroup>) : GetUserGroupsState

    object GetUserGroupsFailure : GetUserGroupsState
