package com.khrd.pingapp.homescreen.changeEmail.usecases

import com.khrd.pingapp.homescreen.changeEmail.ChangeEmailState

interface ChangeEmailUseCase {
    fun changeEmail(newEmail: String, userId: String, password: String, callback: (ChangeEmailState) -> Unit)
}