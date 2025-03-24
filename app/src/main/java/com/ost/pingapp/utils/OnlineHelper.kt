package com.khrd.pingapp.utils

import com.khrd.pingapp.data.users.Online

interface OnlineHelper {
    fun getOnlineOfMultipleDevices(online: List<Online?>?): Online?
}