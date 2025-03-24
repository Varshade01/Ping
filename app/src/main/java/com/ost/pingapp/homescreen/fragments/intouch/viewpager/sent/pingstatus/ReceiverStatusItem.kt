package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus

import com.khrd.pingapp.data.users.Online

data class ReceiverStatusItem(
    var name: String,
    var imageUrl: String?,
    var hasSeen: Boolean,
    var isDeleted: Boolean? = null,
    var isOnline: Online? = null
)