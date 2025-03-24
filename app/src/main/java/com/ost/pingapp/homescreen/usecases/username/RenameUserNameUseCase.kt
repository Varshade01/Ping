package com.khrd.pingapp.homescreen.usecases.username

import com.khrd.pingapp.homescreen.states.RenameUsernameState

interface RenameUserNameUseCase {
    fun renameUsername(newName: String, callback: (RenameUsernameState) -> Unit)
}