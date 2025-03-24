package com.khrd.pingapp.homescreen.fragments

import com.khrd.pingapp.homescreen.adapter.UserItem

data class UsersItemsData(val groupId: String, val users: List<UserItem>, val currentGroup: Boolean)