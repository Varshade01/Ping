package com.khrd.pingapp.pushnotification

import com.khrd.pingapp.data.pings.DatabasePing

interface CreatePushDataUseCase {
    fun createPushData(ping:DatabasePing, callback: (CreatePushState) -> Unit)
}