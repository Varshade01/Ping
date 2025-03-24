package com.khrd.pingapp.groupmanagement.adapter

import com.khrd.pingapp.groupmanagement.states.GroupNameValidationState
import com.khrd.pingapp.groupmanagement.states.JoinGroupError

interface DisplayableItem

data class CreateGroupItem(
    var name: String = "",
    var link: String = "",
    var isNameSaved: Boolean = false,
    var nameValidationError: GroupNameValidationState = GroupNameValidationState.VALID,
    var isInactive: Boolean = true
) : DisplayableItem

data class UpdateGroupImageItem(var groupId: String? = null, var imageUrl: String? = null) : DisplayableItem

data class RenameGroupItem(
    var name: String? = "",
    var isInEditState: Boolean = false,
    var isInSavingState: Boolean = false,
    var nameValidation: GroupNameValidationState = GroupNameValidationState.VALID
) : DisplayableItem

data class JoinGroupItem(
    var joinGroupError: JoinGroupError? = JoinGroupError.LINK_VALID,
    var link: String = ""
) : DisplayableItem

class LeaveGroupItem() : DisplayableItem

class ShareLinkItem() : DisplayableItem

data class MuteGroupItem(
    var muted: Boolean = false,
) : DisplayableItem