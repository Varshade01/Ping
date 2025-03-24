package com.khrd.pingapp.homescreen.fragments

interface BottomSheetListener {
    fun openSendPingDialog(groupId: String, isGroupPing: Boolean)
    fun passGroupId(groupId: String)
    fun muteGroup(groupId: String, isMuted: Boolean)
}