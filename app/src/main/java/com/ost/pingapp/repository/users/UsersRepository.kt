package com.khrd.pingapp.repository.users

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.Online
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.homescreen.listeners.UsersListener

interface UsersRepository {
    suspend fun createUser(id: String, email: String, username: String): UserRequestState
    fun getUser(userId: String, loadFlag: DataLoadFlag, callback: (UserRequestState) -> Unit)
    suspend fun getUserSuspend(userId: String, loadFlag: DataLoadFlag): UserRequestState
    fun getUsers(userIds: List<String>, loadFlag: DataLoadFlag, callback: (List<DatabaseUser>) -> Unit)
    fun updateUsername(id: String, username: String, callback: (UserRequestState) -> Unit)
    fun updateUserJob(id: String, job: String, callback: (UserRequestState) -> Unit)
    fun deleteUser(id: String, callback: (UserRequestState) -> Unit)
    fun deleteUserGroup(id: String, groupId: String, callback: (UserRequestState) -> Unit)
    fun addUserGroup(id: String, groupId: String, callback: (UserRequestState) -> Unit)
    fun updateEmail(id: String, email: String, callback: (UserRequestState) -> Unit)
    fun getUserGroups(id: String, loadFlag: DataLoadFlag, callback: (UserRequestState) -> Unit)
    fun getGroupMembers(groupId: String, loadFlag: DataLoadFlag)
    fun updateFcmToken(id: String, token: String)
    fun updateUserJobInCache(id: String, job: String)
    fun updateUsernameCache(id: String, username: String)
    fun updateUserOnlineStatus(id: String, status: Online)
    fun updateProfilePhoto(id: String, bytes: ByteArray, callback: (UserRequestState) -> Unit)
    fun deleteProfilePhoto(id: String, callback: (UserRequestState) -> Unit)
    fun removeFcmToken(id: String, token: String)
    fun subscribeForUsersChanges(usersIds: List<String>, listener: () -> Unit)
    fun unSubscribeUsersChanges(listener: () -> Unit)
    fun addListener(groupId: String, listener: UsersListener)
    fun removeListener(listener: UsersListener)
    fun muteItem(currentUserId: String, itemId: String, callback: ((UserRequestState) -> Unit)? = null)
    fun unMuteItem(currentUserId: String, itemId: String, callback: ((UserRequestState) -> Unit)? = null)
    fun hideUserInfo(currentUserId: String, isHide: Boolean, callback: (UserRequestState) -> Unit)
}