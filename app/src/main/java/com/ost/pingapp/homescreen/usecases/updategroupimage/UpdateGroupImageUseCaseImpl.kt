package com.khrd.pingapp.homescreen.usecases.updategroupimage

import android.net.Uri
import com.khrd.pingapp.data.groups.GroupImageUpdateOfflineURI
import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.repository.groups.GroupRepository

class UpdateGroupImageUseCaseImpl(
    private val groupRepository: GroupRepository,
    private val connectionStatus: ConnectionStatus,
) : UpdateGroupImageUseCase {
    override fun updateGroupImage(groupId: String, bytes: ByteArray, uri: Uri, callback: (GroupState) -> Unit) {
        if (connectionStatus.getConnectionStatus()) {
            groupRepository.updateGroupImage(groupId, bytes) { state -> callback(state) }
        } else {
            callback(GroupImageUpdateOfflineURI(groupId, uri))
        }
    }
}