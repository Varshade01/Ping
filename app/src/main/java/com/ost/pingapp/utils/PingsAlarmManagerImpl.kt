package com.khrd.pingapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import com.khrd.pingapp.alarmManager.ScheduledPingsReceiver

class PingsAlarmManagerImpl : PingsAlarmManager {
    override fun setAlarm(context: Context?, time: Long, pingId: String) {
        val alarmManager = context?.applicationContext?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context.applicationContext, ScheduledPingsReceiver::class.java)
        intent.action = pingId
        val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, time.toInt(), intent, FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }
    }
}