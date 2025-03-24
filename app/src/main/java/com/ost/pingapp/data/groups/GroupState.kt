package com.khrd.pingapp.data.groups

import android.net.Uri

sealed interface IGroupState

sealed class GroupState(): IGroupState

data class GroupSuccess(val group: DatabaseGroup): GroupState()

data class GroupOfflineState(val group: DatabaseGroup): GroupState()

data class GroupImageUpdateOfflineURI(val groupId: String, val uri: Uri) : GroupState()

data class GroupFailure(val error: GroupError): GroupState()

enum class GroupError {
    UNEXISTING_GROUP,
    UNKNOWN_ERROR,
    NETWORK_ERROR
}
