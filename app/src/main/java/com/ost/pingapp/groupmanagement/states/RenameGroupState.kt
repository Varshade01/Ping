package com.khrd.pingapp.groupmanagement.states

sealed class RenameGroupState

object RenameGroupEditAction : RenameGroupState()
data class RenameGroupFailure(val error: RenameGroupError) : RenameGroupState()
data class RenameGroupSaveAction(
    val validation: GroupNameValidationState = GroupNameValidationState.VALID,
    val name: String = ""
) : RenameGroupState()

data class RenameGroupOfflineSaveAction(
    val groupId: String,
    val validation: GroupNameValidationState = GroupNameValidationState.VALID,
    val name: String = ""
) : RenameGroupState()

enum class RenameGroupError() {
    RENAME_GROUP_FAILED,
    NETWORK_ERROR
}