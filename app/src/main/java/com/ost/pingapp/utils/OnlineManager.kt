package com.khrd.pingapp.utils

interface OnlineManager {
    fun start()
    fun stop(currentUserId: String? = null)
}