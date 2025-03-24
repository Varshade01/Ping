package com.khrd.pingapp.homescreen.usecases

interface UpdateFcmTokenUseCase {
    fun updateFcmToken(token: String? = null)
}