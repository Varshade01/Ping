package com.khrd.pingapp.homescreen.changePassword

sealed interface IChangePasswordState

sealed class ChangePasswordState(): IChangePasswordState

class  ChangePasswordSuccess: ChangePasswordState()

data class ChangePasswordFailure(val error: UpdatePasswordErrors): ChangePasswordState()