package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.homescreen.states.GetUserGroupsState

interface GetGroupsForSendPingDialogUseCase {
    fun loadGroups(callback: (GetUserGroupsState) -> Unit)
}