package com.khrd.pingapp.utils

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.pings.PingData

class PingConverter {
    fun convertDatabasePing(pingData: PingData): DatabasePing {
        val receiversMap: HashMap<String, String> = hashMapOf()
        pingData.receivers.forEach {
            if (it.isNotBlank()) {
                receiversMap[it] = "${it}_${pingData.timestamp}"
            }
        }
        return DatabasePing(
            id = pingData.timestamp.toString(),
            timestamp = pingData.timestamp,
            from = hashMapOf(pingData.from to "${pingData.from}_${pingData.timestamp}"),
            receivers = receiversMap,
            message = pingData.emoji,
            groupId = pingData.groupId,
            scheduledTime = pingData.scheduledTime?.time,
            groupFrom = pingData.groupFrom ?: "",
            recurringTime = pingData.recurringTime
        )
    }
}