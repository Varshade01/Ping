package com.khrd.pingapp.data.pings

import com.khrd.pingapp.R
import java.util.*

data class PingData(
    var pingId: String? = null,
    var from: String = "",
    var receivers: List<String> = listOf(),
    var emoji: String = "",
    var groupId: String? = null,
    var scheduledTime: Date? = null,
    var timestamp: Long? = null,
    var groupFrom: String? = null,
    var recurringTime: RecurringTime = RecurringTime.NO_REPEAT
)

enum class RecurringTime {
    DAY, WEEK, MONTH, YEAR, NO_REPEAT
}

fun RecurringTime.getResId(): Int {
    return when (this) {
        RecurringTime.DAY -> R.string.daily
        RecurringTime.WEEK -> R.string.weekly
        RecurringTime.MONTH -> R.string.monthly
        RecurringTime.YEAR -> R.string.yearly
        RecurringTime.NO_REPEAT -> R.string.no_repeat
    }
}