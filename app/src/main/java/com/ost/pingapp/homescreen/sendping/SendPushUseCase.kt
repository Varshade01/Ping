package com.khrd.pingapp.homescreen.sendping

import com.khrd.pingapp.homescreen.states.SendPushState
import com.khrd.pingapp.pushnotification.PushNotification

interface SendPushUseCase {
    suspend fun sendPush(body: PushNotification): SendPushState
}