package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases

import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetReceiversStatusState

interface ConvertToReceiverStatusItemsUseCase {
    fun convertToReceiverStatusItems(receivers: List<UserItem>, listOfSeen: List<String>, callback: (GetReceiversStatusState) -> Unit)
}