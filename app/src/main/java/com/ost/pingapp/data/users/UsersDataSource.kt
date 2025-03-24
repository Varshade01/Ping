package com.khrd.pingapp.data.users

import com.khrd.pingapp.homescreen.listeners.UsersListener

interface UsersDataSource {
    suspend fun createUser(id: String, email: String, username: String): UserRequestState
    fun getUser(id: String, callback: (UserRequestState) -> Unit)
    fun updateUsername(id: String, username: String, callback: (UserRequestState) -> Unit)
    fun updateUserJob(id: String, job: String, callback: (UserRequestState) -> Unit)
    fun updateUserOnlineStatus(id: String, deviceId: String, status: Online)
    fun deleteUser(id: String, callback: (UserRequestState) -> Unit)
    fun getGroups(id: String, callback: (UserRequestState) -> Unit)
    fun deleteUserGroup(id: String, groupId: String, callback: (UserRequestState) -> Unit)
    fun addUserGroup(id: String, groupId: String, callback: (UserRequestState) -> Unit)
    fun updateEmail(id: String, email: String, callback: (UserRequestState) -> Unit)
    fun updateProfilePhoto(id: String, bytes: ByteArray, callback: (UserRequestState) -> Unit)
    fun deleteProfilePhoto(id: String, callback: (UserRequestState) -> Unit)
    fun updateFcmToken(id: String, token: String)
    fun removeFcmToken(id: String, token: String)
    fun getGroupMembers(groupId: String)
    fun setListener(listener: UsersListener)
    fun removeUsersValueListeners()
    fun muteItem(currentUserId: String, userId: String, callback: (UserRequestState) -> Unit)
    fun unMuteItem(currentUserId: String, userId: String, callback: (UserRequestState) -> Unit)
    fun hideUserInfo(currentUserId: String, isHide: Boolean, callback: (UserRequestState) -> Unit)
}