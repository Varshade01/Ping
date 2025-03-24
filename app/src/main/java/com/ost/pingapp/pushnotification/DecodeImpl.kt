package com.khrd.pingapp.pushnotification

import android.util.Base64

    fun String.decode(): String {
        return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
    }

