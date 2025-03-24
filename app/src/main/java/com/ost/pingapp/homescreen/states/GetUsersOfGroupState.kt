package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.homescreen.adapter.UserItem

interface IGetUsersOfGroupState

sealed class GetUsersOfGroupState: IGetUsersOfGroupState

data class  GetUsersOfGroupSuccess(val items: List<UserItem>?): GetUsersOfGroupState()

object GetUsersOfGroupFailure : GetUsersOfGroupState()