package com.khrd.pingapp.homescreen.usecases.pings

interface SubscribeToUserChangesUseCase {
    fun subscribeForUsersChanges(usersIds: List<String>, listener: () -> Unit)
    fun unSubscribeForUsersChanges(listener: () -> Unit)
}