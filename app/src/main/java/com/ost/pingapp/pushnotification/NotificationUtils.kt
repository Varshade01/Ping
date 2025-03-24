package com.khrd.pingapp.pushnotification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.ReceivedPingItem
import javax.inject.Inject

class NotificationUtils @Inject constructor(
    private val notificationManager: NotificationManager,
    private val context: Context
) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNewPingsChannel()
            createSyncingPingsChannel()
        }
    }

    private fun createSyncingPingsChannel() {
        val notificationChannel = NotificationChannel(
            context.getString(R.string.syncing_pings_channel_id),
            context.getString(R.string.syncing_pings_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationChannel.description = context.getString(R.string.description_syncing_channel)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createNewPingsChannel() {
        val notificationChannel = NotificationChannel(
            context.getString(R.string.new_ping_channel_id),
            context.getString(R.string.new_ping_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableVibration(true)
        notificationChannel.description = context.getString(R.string.description_syncing_channel)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun createSyncingPingsForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(0, showSyncingPingsNotification())
    }

    fun showSyncingPingsNotification(): Notification {
        val notification = NotificationCompat.Builder(context, context.getString(R.string.syncing_pings_channel_id))
            .setContentTitle(context.getString(R.string.syncing_pings_title))
            .setSmallIcon(R.drawable.ic_ping)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
        return notification.build()
    }

    fun showNewPingNotification(
        ping: ReceivedPingItem
    ) {
        val resultIntent = Intent(context, HomeScreen::class.java)
        resultIntent.putExtra(Constants.OPEN_PINGS_INTENT_KEY, true)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
//SCHEDULED Ping
        val title = ping.groupFrom?.name?.let { "[$it] ${ping.userItem?.fullname}" } ?: ping.userItem?.fullname
        val newMessageNotification = NotificationCompat.Builder(context, context.getString(R.string.new_ping_channel_id))
            .setContentTitle(title)
            .setContentText(ping.emoji)
            .setSmallIcon(R.drawable.arrow_down)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .setGroup(context.getString(R.string.new_ping))
        notificationManager.notify(ping.date.toInt(), newMessageNotification.build())
    }

    fun createGroupNotification(unreadPingsSize: Int) {
        val groupBuilder = NotificationCompat.Builder(context, context.getString(R.string.new_ping_channel_id))
            .setSmallIcon(R.drawable.arrow_down)
            .setSubText(if (unreadPingsSize == 1) "There is $unreadPingsSize new ping" else "There are $unreadPingsSize new pings")
            .setGroup(context.getString(R.string.new_ping))
            .setAutoCancel(true)
            .setGroupSummary(true)
        notificationManager.notify(10, groupBuilder.build())
    }
}