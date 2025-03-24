package com.khrd.pingapp.groupmanagement.states

sealed interface ILeaveGroupState

object LeaveGroupAction : ILeaveGroupState

data class LeaveGroupFailure(val error: LeaveGroupError) : ILeaveGroupState

object LeaveGroupOffline : ILeaveGroupState

enum class LeaveGroupError() {
    LEAVE_GROUP_FAILED,
    NETWORK_ERROR
}

