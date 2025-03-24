package com.khrd.pingapp.utils.deviceIdHelper

import android.content.Context
import android.provider.Settings

class DeviceIdHelperImpl(val context: Context) : DeviceIdHelper {
    override fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}