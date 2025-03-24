package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases

import androidx.core.text.toSpannable
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.pings.RecurringTime
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.Online
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.GetGroupsByIdsUseCase
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.DisplayablePingItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingScheduledItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders.GetPingsState
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders.GetPingsSuccess
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCase
import com.khrd.pingapp.utils.OnlineHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConvertToSentPingItemUseCaseImpl @Inject constructor(
    private val getUsersByIdUseCase: GetUsersByIdUseCase,
    private val getGroupsByIdsUseCase: GetGroupsByIdsUseCase,
    private val firebaseAuth: FirebaseAuthAPI,
    private val getGroupUseCase: GetGroupUseCase,
    @IoCoroutineScope private val coroutineScope: CoroutineScope,
    private val onlineHelper: OnlineHelper,
    private val usersRepository: UsersRepository
) : ConvertToSentPingItemUseCase {
    override fun convertToSentPing(pings: List<DatabasePing>, callback: (GetPingsState) -> Unit) {
        val allUsers = mutableSetOf<String>()
        pings.forEach { allUsers.addAll(it.receivers.keys) }
        getUsersByIdUseCase.getUsers(allUsers.toList(), DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { members ->
            val groupIds = pings.mapNotNull { it.groupId }
            getGroupsByIdsUseCase.getGroups(groupIds = groupIds, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { groups ->
                coroutineScope.launch {
                    val pingItems = getPingItems(pings, members, groups)
                    // TODO remove offset when scheduled cache is done
                    val offset = if (pings.isNotEmpty()) pings.first().from.values.first() else ""
                    withContext(Dispatchers.Main) {
                        callback(GetPingsSuccess(pingItems, offset))
                    }
                }
            }
        }
    }

    private suspend fun getPingItems(
        pings: List<DatabasePing>,
        allMembers: List<DatabaseUser>,
        groups: List<DatabaseGroup>
    ): List<DisplayablePingItem> {

        val pingItems: MutableList<DisplayablePingItem> = mutableListOf()
        val currentUser = getCurrentUser(firebaseAuth.currentUserId())
        pings.forEach { ping ->

            var groupFrom: DatabaseGroup? = null
            val requestGroupState = getGroupUseCase.getGroupSuspend(ping.groupFrom, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
            if (requestGroupState is GroupSuccess) {
                groupFrom = requestGroupState.group
            }

            val pingReceivers = allMembers.filter { ping.receivers.keys.contains(it.id) }
            if (!ping.isGroupPing()) {
                pingItems.add(createPing(ping, pingReceivers, groupFrom, currentUser))
            } else {
                val group = groups.find { it.id == ping.groupId }
                if (group == null) {
                    pingItems.add(createPing(ping, currentUser = currentUser))
                } else {
                    pingItems.add(
                        createPing(
                            ping = ping,
                            receiver = pingReceivers.filterNot { it.id == firebaseAuth.currentUserId() },
                            groupFrom = group,
                            currentUser = currentUser
                        )
                    )
                }
            }
        }
        return pingItems
    }

    private suspend fun createPing(
        ping: DatabasePing,
        receiver: List<DatabaseUser> = emptyList(),
        groupFrom: DatabaseGroup? = null,
        currentUser: DatabaseUser? = null
    ): DisplayablePingItem =
        if (ping.isUnscheduled()) {
            SentPingItem(
                receiver = mapDatabaseUserToUserItem(receiver, currentUser),
                date = ping.timestamp!!,
                emoji = ping.message,
                groupId = ping.groupId,
                views = filterPingViews(ping),
                groupFrom = groupFrom,
                online = getOnline(receiver)
            )
        } else {
            SentPingScheduledItem(
                receiver = mapDatabaseUserToUserItem(receiver, currentUser),
                scheduledDate = ping.scheduledTime!!,
                emoji = ping.message,
                groupId = ping.groupId,
                pingId = ping.id,
                timestamp = ping.timestamp!!,
                groupFrom = groupFrom,
                online = getOnline(receiver),
                recurringTime = ping.recurringTime
            )
        }

    private suspend fun mapDatabaseUserToUserItem(users: List<DatabaseUser>, currentUser: DatabaseUser?): MutableList<UserItem> {
        val userItemList = mutableListOf<UserItem>()
        users.forEach { user ->
            val userGroups = getUserGroups(user)
            val online = onlineHelper.getOnlineOfMultipleDevices(user.online?.values?.toList())
            val userItem = UserItem(
                user.username?.toSpannable(),
                user.job,
                user.photoURL,
                user.id,
                userGroups,
                online,
                muted = currentUser?.mutedItems?.containsKey(user.id) == true,
                isDeleted = user.isDeleted,
                isHide = user.hideInfo
            )

            userItemList.add(userItem)
        }
        return userItemList
    }

    private suspend fun getUserGroups(databaseUser: DatabaseUser): MutableList<DatabaseGroup?> {
        val userGroups = mutableListOf<DatabaseGroup?>()
        databaseUser.groups?.keys?.forEach { groupId ->
            val requestGroupState = getGroupUseCase.getGroupSuspend(groupId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
            if (requestGroupState is GroupSuccess) {
                userGroups.add(requestGroupState.group)
            }
        }
        return userGroups
    }

    private fun getOnline(databaseUser: List<DatabaseUser>): Online? = if (databaseUser.size == 1) {
        onlineHelper.getOnlineOfMultipleDevices(databaseUser.first().online?.values?.toList())
    } else null

    private fun DatabasePing.isGroupPing() = !groupId.isNullOrBlank()

    private fun DatabasePing.isUnscheduled() = scheduledTime == null

    private fun filterPingViews(ping: DatabasePing) =
        if (!ping.isGroupPing()) ping.views.keys.toList()
        else ping.views.keys.toList().filterNot { it == firebaseAuth.currentUserId() }

    private suspend fun getCurrentUser(userId: String?): DatabaseUser? {
        if (userId != null) {
            val requestState = usersRepository.getUserSuspend(userId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
            if (requestState is UserRequestState.UserRequestSuccess) {
                return requestState.user
            }
        }
        return null
    }
}

