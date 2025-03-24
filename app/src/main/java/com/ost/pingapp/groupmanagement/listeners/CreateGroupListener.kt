package com.khrd.pingapp.groupmanagement.listeners

import com.khrd.pingapp.groupmanagement.states.CreateGroupState


interface CreateGroupListener {
    fun onCreateGroupNameChanged(name: String)
    fun onCreateGroupAction(action: CreateGroupState)
}