package com.khrd.pingapp.homescreen.usecases

interface OnlineHandlerUseCase {
    fun sendOnlineStatus(status: Boolean, currentUserId: String? = null)
}