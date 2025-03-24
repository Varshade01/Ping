package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.users.DatabaseUser

interface IGetUserState

sealed class GetUserState : IGetUserState

data class GetUserSuccess(val user: DatabaseUser) : GetUserState()

object GetUserFailure : GetUserState()