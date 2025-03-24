package com.khrd.pingapp.homescreen.usecases.pings.received

import android.os.ConditionVariable
import androidx.core.text.toSpannable
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.ReceivedPingItem
import com.khrd.pingapp.homescreen.states.LoadReceivedPingsState
import com.khrd.pingapp.homescreen.states.LoadReceivedPingsSuccess
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCase
import com.khrd.pingapp.utils.OnlineHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class ConvertToReceivedPingUseCaseImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuthAPI,
    private val getUsersByIdUseCase: GetUsersByIdUseCase,
    private val getGroupUseCase: GetGroupUseCase,
    @IoCoroutineScope private val coroutineScope: CoroutineScope,
    private val onlineHelper: OnlineHelper,
    private val usersRepository: UsersRepository
) : ConvertToReceivedPingUseCase {
    override fun convertToReceivedPing(pings: List<DatabasePing>, loadFlag: DataLoadFlag, callback: (LoadReceivedPingsState) -> Unit) {
        val userId = firebaseAuth.currentUserId() ?: ""

        val pingUserIds = pings.map { it.from.keys.first() }

        getUsersByIdUseCase.getUsers(pingUserIds, loadFlag) { usersList ->
            coroutineScope.launch {
                val result = mutableListOf<ReceivedPingItem>()
                val currentUser = getCurrentUser(userId, loadFlag)
                pings.forEach { ping ->
                    val sender = usersList.find { ping.from.keys.first() == it.id }

                    if (sender != null) {
                        result.add(
                            ReceivedPingItem(
                                id = ping.id,
                                userItem = mapDatabaseUserToUserItem(sender, currentUser),
                                date = ping.timestamp ?: 0,
                                emoji = ping.message,
                                isGroupPing = !ping.groupId.isNullOrBlank(),
                                seen = ping.views.containsKey(userId),
                                groupFrom = DatabaseGroup(ping.groupFrom),
                            )
                        )
                    } else {
                        result.add(
                            ReceivedPingItem(
                                id = ping.id,
                                date = ping.timestamp ?: 0,
                                emoji = ping.message,
                                isGroupPing = !ping.groupId.isNullOrBlank(),
                                seen = ping.views.containsKey(userId),
                                groupFrom = DatabaseGroup(ping.groupFrom),
                                userItem = null
                            )
                        )
                    }
                }
                result.forEach { resultPing ->
                    val requestGroupState = getGroupUseCase.getGroupSuspend(resultPing.groupFrom?.id, loadFlag)
                    if (requestGroupState is GroupSuccess){
                        resultPing.groupFrom = requestGroupState.group
                    }
                }
                withContext(Dispatchers.Main) {
                    callback(LoadReceivedPingsSuccess(result.sortedByDescending { it.date }))
                }
            }
        }
    }

    private suspend fun mapDatabaseUserToUserItem(user: DatabaseUser, currentUser: DatabaseUser?): UserItem {
        val userGroups = getUserGroups(user)
        val online = onlineHelper.getOnlineOfMultipleDevices(user.online?.values?.toList())
        return UserItem(
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
    }

    private suspend fun getUserGroups(databaseUser: DatabaseUser): MutableList<DatabaseGroup?> {
        val userGroups = mutableListOf<DatabaseGroup?>()
        databaseUser.groups?.keys?.forEach { groupId ->
            val requestGroupState = getGroupUseCase.getGroupSuspend(groupId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
            if (requestGroupState is GroupSuccess){
                userGroups.add(requestGroupState.group)
            }
        }
        return userGroups
    }

    private suspend fun getCurrentUser(userId: String, loadFlag: DataLoadFlag): DatabaseUser? {
        if (userId.isNotBlank()) {
            val requestState = usersRepository.getUserSuspend(userId, loadFlag)
            if (requestState is UserRequestState.UserRequestSuccess) {
                return requestState.user
            }
        }
        return null
    }
}