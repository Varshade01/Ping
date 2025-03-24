package com.khrd.pingapp.useradministration.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser

interface GetUsersByIdUseCase {
    fun getUsers(userIds: List<String>, loadFlag: DataLoadFlag, callback: (List<DatabaseUser>) -> Unit)
}