package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.homescreen.listeners.UsersListener

interface GetGroupMembersUseCase {
    fun getGroupMembers(groupId: String, loadFlag: DataLoadFlag)
    fun addListener(groupId: String, listener: UsersListener)
    fun removeListener(listener: UsersListener)
}