package com.khrd.pingapp.pushnotification

import android.content.Context
import android.util.Log
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.homescreen.states.LoadReceivedPingsFailure
import com.khrd.pingapp.homescreen.states.LoadReceivedPingsSuccess
import com.khrd.pingapp.homescreen.usecases.pings.received.ConvertToReceivedPingUseCase
import javax.inject.Inject

class ShowPushNotificationUseCaseImpl @Inject constructor(
    val convertPingsUseCase: ConvertToReceivedPingUseCase,
    val notificationUtils: NotificationUtils
) : ShowPushNotificationUseCase {
    override fun showPushNotification(ping: DatabasePing, unreadPingsSize: Int, context: Context) {
        convertPingsUseCase.convertToReceivedPing(listOf(ping), DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { state ->
            when (state) {
                is LoadReceivedPingsSuccess -> {
                    notificationUtils.showNewPingNotification(state.pings[0])
                    notificationUtils.createGroupNotification(unreadPingsSize)
                }
                is LoadReceivedPingsFailure -> {
                    Log.d("*****", "Creating notification failed")
                }
            }
        }
    }
}