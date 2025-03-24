package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.data.users.UserRequestState

interface HideUserInformationUseCase {
    fun hideUserInfo(currentUserId: String, isHide: Boolean, callback: (UserRequestState) -> Unit)
}