package com.khrd.pingapp.groupmanagement.listeners

import com.khrd.pingapp.groupmanagement.states.RenameGroupState

interface RenameGroupListener {
    fun onRenameGroupAction(state: RenameGroupState)
}