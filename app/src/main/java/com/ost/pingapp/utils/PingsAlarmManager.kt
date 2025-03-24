package com.khrd.pingapp.utils

import android.content.Context

interface PingsAlarmManager {
    fun setAlarm(context: Context?, time: Long, pingId: String)
}