package com.khrd.pingapp.utils

import com.khrd.pingapp.constants.Constants.ONE_MINUTE
import com.khrd.pingapp.data.users.Online

class OnlineHelperImpl : OnlineHelper {

    override fun getOnlineOfMultipleDevices(online: List<Online?>?): Online? = if (!online.isNullOrEmpty()) {
        val sorted = online.sortedByDescending { it?.timestamp }
        sorted.find { it?.status == true && System.currentTimeMillis() - it.timestamp <= ONE_MINUTE } ?: sorted.firstOrNull()
            ?.apply { status = false }
    } else null
}
