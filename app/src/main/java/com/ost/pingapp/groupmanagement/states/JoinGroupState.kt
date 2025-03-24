package com.khrd.pingapp.groupmanagement.states


sealed class JoinGroupState

data class JoinGroupAction(val link: String) : JoinGroupState()

data class JoinGroupFailure(val error: JoinGroupError) : JoinGroupState()

data class JoinGroupWithConfirmationAction(val name: String? = "") : JoinGroupState()

data class JoinByOutsideLinkConfirmedState(val name: String? = ""): JoinGroupState()


enum class JoinGroupError {
    LINK_VALID,
    LINK_INVALID,
    EMPTY_FIELD,
    UNEXISTING_GROUP,
    SAME_GROUP_ERROR,
    UNKNOWN_ERROR,
    OUTSIDE_LINK_SAME_GROUP_ERROR,
    OUTSIDE_LINK_INVALID,
    NETWORK_ERROR
}
