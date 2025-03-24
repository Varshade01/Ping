package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.homescreen.states.GetUserState

interface GetUserUseCase {
    fun getUser(userId: String?, loadFlag: DataLoadFlag, callback: (GetUserState) -> Unit)
}