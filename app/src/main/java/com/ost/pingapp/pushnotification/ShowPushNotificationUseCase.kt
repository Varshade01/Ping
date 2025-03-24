package com.khrd.pingapp.pushnotification

import android.content.Context
import com.khrd.pingapp.data.pings.DatabasePing

interface ShowPushNotificationUseCase {
    fun showPushNotification(ping: DatabasePing, unreadPingsSize: Int, context: Context)
}