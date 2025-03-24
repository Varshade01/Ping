package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetReceiversStatusState
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetReceiversStatusSuccess
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.ReceiverStatusItem
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCase
import com.khrd.pingapp.utils.OnlineHelper

class ConvertToReceiverStatusItemsUseCaseImpl(
    private val getUsersByIdUseCase: GetUsersByIdUseCase,
    private val onlineHelper: OnlineHelper
) : ConvertToReceiverStatusItemsUseCase {

    override fun convertToReceiverStatusItems(
        receivers: List<UserItem>,
        listOfSeen: List<String>,
        callback: (GetReceiversStatusState) -> Unit
    ) {
        val receiversIds = receivers.map { it.userId }
        val receiverStatusItems = receivers.map { member -> member.toReceiverStatusItem(listOfSeen) }.toMutableList()
        // If members of a group contains all users who have seen ping return receiverStatusItems
        if (receiversIds.containsAll(listOfSeen)) {
            callback(GetReceiversStatusSuccess(receiverStatusItems.filter { it.hasSeen } + receiverStatusItems.filter { !it.hasSeen }))
        } else {
            //Else find ids of all users who have seen the ping but are not in group members
            val userIdsNotInGroup = listOfSeen.filterNot { receiversIds.contains(it) }
            //Load users, convert to receiverStatusItems and add to the list
            getUsersByIdUseCase.getUsers(userIdsNotInGroup, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { users ->
                receiverStatusItems.addAll(users.map { user ->
                    if (user.isDeleted != null && user.isDeleted!!) {
                        ReceiverStatusItem("", null, true, user.isDeleted)
                    } else {
                        ReceiverStatusItem(
                            user.username!!,
                            user.photoURL,
                            true,
                            user.isDeleted,
                            onlineHelper.getOnlineOfMultipleDevices(user.online?.values?.toList())
                        )
                    }
                })
                callback(GetReceiversStatusSuccess(receiverStatusItems.filter { it.hasSeen } + receiverStatusItems.filter { !it.hasSeen }))
            }
        }
    }

    private fun UserItem.toReceiverStatusItem(listOfSeen: List<String>) =
        ReceiverStatusItem(
            (this.fullname?.let { it.toString() } ?: ""),
            this.photoURL,
            listOfSeen.contains(this.userId),
            this.isDeleted,
            onlineHelper.getOnlineOfMultipleDevices(listOf(this.isOnline))
        )
}