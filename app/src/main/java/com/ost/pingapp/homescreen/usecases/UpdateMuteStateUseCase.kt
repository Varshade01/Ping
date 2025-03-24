package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.data.users.UserRequestState

interface UpdateMuteStateUseCase {
    fun muteItem(currentUserId: String, itemId: String, callback: ((UserRequestState) -> Unit)? = null)
    fun unMuteItem(currentUserId: String, itemId: String, callback: ((UserRequestState) -> Unit)? = null)
}