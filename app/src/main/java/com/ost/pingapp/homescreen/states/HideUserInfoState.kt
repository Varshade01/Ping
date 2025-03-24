package com.khrd.pingapp.homescreen.states

sealed interface IHideUserInfoState

sealed class HideUserInfoState : IHideUserInfoState

class HideUserInfoFailure() : HideUserInfoState()

class HideUserInfoSuccess(var isHide: Boolean) : HideUserInfoState()