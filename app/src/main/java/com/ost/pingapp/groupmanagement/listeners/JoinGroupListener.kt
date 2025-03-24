package com.khrd.pingapp.groupmanagement.listeners

interface JoinGroupListener {
    fun onJoinGroupAction()
    fun onJoinGroupLinkChanged(link: String)
}