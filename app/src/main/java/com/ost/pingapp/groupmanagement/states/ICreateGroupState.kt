package com.khrd.pingapp.groupmanagement.states

sealed interface ICreateGroupState

sealed class CreateGroupState : ICreateGroupState

object CreateGroupSaveAction : CreateGroupState()

data class CreateGroupEditAction(val state: GroupNameValidationState, val link: String) : CreateGroupState()

object CreateGroupDoneAction : CreateGroupState()

data class CreateGroupCopyLinkAction(val link: String?) : CreateGroupState()

data class CreateGroupFailureState(val error: CreateGroupActionError) : CreateGroupState()

data class CreateGroupConfirmationAction(val name: String? = ""): CreateGroupState()

enum class CreateGroupActionError {
    CREATE_GROUP_FAILED,
    GENERATE_LINK_FAILED,
    RENAME_GROUP_FAILED,
    LEAVE_PREVIOUS_GROUP_FAILED,
    NETWORK_ERROR
}