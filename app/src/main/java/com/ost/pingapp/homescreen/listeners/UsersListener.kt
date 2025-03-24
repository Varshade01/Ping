package com.khrd.pingapp.homescreen.listeners

import com.khrd.pingapp.data.users.DatabaseUser

abstract class UsersListener {
    private var currentCheckSum: Long = -1

    abstract fun onUsersChanged(groupId: String, users: List<DatabaseUser>?)
    fun setNewUsers(groupId: String, users: List<DatabaseUser>?) {
        var newCheckSum: Long = 0
        users?.forEach {
            newCheckSum += it.hashCode()
        }
//TODO temporary disable checksum checking
//        if (currentCheckSum != newCheckSum) {
        onUsersChanged(groupId, users)
        currentCheckSum = newCheckSum
//        }
    }
}