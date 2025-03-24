package com.khrd.pingapp.groupmanagement.listeners

import com.khrd.pingapp.groupmanagement.adapter.MuteGroupItem

interface MuteGroupListener {
    fun onMuteGroupClicked(groupItem: MuteGroupItem)
}