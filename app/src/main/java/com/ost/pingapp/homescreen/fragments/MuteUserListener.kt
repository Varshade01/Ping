package com.khrd.pingapp.homescreen.fragments

import com.khrd.pingapp.homescreen.adapter.UserItem

interface MuteUserListener {
    fun onMuteUserClicked(user: UserItem)
}