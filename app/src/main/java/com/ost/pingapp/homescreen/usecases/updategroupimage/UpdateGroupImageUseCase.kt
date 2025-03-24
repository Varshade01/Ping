package com.khrd.pingapp.homescreen.usecases.updategroupimage

import android.net.Uri
import com.khrd.pingapp.data.groups.GroupState

interface UpdateGroupImageUseCase {
    fun updateGroupImage(groupId: String, bytes: ByteArray, uri: Uri, callback: (GroupState) -> Unit)
}