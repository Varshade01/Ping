package com.khrd.pingapp.groupmanagement.usecases.createGroup

import com.khrd.pingapp.data.groups.GroupState

interface GenerateLinkUseCase {
   fun generateLink(groupId: String, callback: (GroupState) -> Unit)
}