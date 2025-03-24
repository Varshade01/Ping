package com.khrd.pingapp.homescreen.changePassword

interface ChangePasswordUseCase {
    fun changePassword(oldPassword: String, newPassword: String, callback: (UpdatePasswordState) -> Unit)
}