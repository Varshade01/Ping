package com.khrd.pingapp.data.groups

import com.khrd.pingapp.data.users.DatabaseUser

sealed interface IUserState

sealed class UserState() : IUserState

data class UserSuccess(val users: MutableList<DatabaseUser>) : UserState()

class UserFailure():UserState()
