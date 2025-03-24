package com.khrd.pingapp.homescreen.fragments

import com.khrd.pingapp.homescreen.adapter.UserItem

interface SendPingToUserListener {
    fun onSendPingToUserClicked(user: UserItem)
}