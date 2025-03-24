package com.khrd.pingapp.groupmanagement.listeners

import android.net.Uri

interface GroupImageOfflineUpdateHelper {
    fun removeTempImageFile(uri: Uri)
    fun removeTempImageFile(path: String)
}