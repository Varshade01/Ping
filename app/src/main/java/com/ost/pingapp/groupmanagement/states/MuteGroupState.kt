package com.khrd.pingapp.groupmanagement.states

sealed interface MuteGroupState

data class MuteGroupSuccess(val groupId: String? = null, val isMuted: Boolean) : MuteGroupState

data class MuteGroupFailure(val error: MuteGroupError) : MuteGroupState

enum class MuteGroupError() {
    MUTE_GROUP_FAILED,
    NETWORK_ERROR
}