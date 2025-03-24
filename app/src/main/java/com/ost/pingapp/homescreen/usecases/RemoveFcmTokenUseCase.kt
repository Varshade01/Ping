package com.khrd.pingapp.homescreen.usecases

interface RemoveFcmTokenUseCase {
    fun removeFcmToken(currentUserId: String? = null)
}