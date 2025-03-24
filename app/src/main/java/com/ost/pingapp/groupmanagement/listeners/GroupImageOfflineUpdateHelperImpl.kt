package com.khrd.pingapp.groupmanagement.listeners

import android.net.Uri
import android.util.Log
import java.io.File

class GroupImageOfflineUpdateHelperImpl : GroupImageOfflineUpdateHelper {
    override fun removeTempImageFile(uri: Uri) {
        val deleted = uri.path?.let { File(it).delete() }
        Log.i(TEMP_FILE_REMOVING_TAG, "onUpdateGroupImageOfflineAction: deleted by Uri: $deleted")
    }

    override fun removeTempImageFile(path: String) {
        val imageFile = File(path)
        val deleted = if (imageFile.exists()) {
            imageFile.delete()
        } else {
            false
        }
        Log.i(TEMP_FILE_REMOVING_TAG, "onUpdateGroupImageOfflineAction: deleted by Path: $deleted")
    }

    companion object {
        private const val TEMP_FILE_REMOVING_TAG = "TEMP_FILE_REMOVING"
    }

}