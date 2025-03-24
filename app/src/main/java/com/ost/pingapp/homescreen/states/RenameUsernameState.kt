package com.khrd.pingapp.homescreen.states

sealed interface IRenameUsernameState

sealed class RenameUsernameState: IRenameUsernameState

object RenameUsernameStateSuccess: RenameUsernameState()

object RenameUsernameOfflineState: RenameUsernameState()

object RenameUsernameStateFailure: RenameUsernameState()