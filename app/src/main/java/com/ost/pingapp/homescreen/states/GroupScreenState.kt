package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.groups.DatabaseGroup

data class GroupScreenState(
    val databaseGroup: DatabaseGroup? = null,
    val isGroupEmpty: Boolean = true,
    val isSearchCollapsed: Boolean = true,
    val userHasOneGroup: Boolean = true,
    val groupLoaded: Boolean = false,
    val isMuted: Boolean = false,
)