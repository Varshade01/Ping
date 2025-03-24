package com.khrd.pingapp.groupmanagement.usecases.joinGroup

import com.khrd.pingapp.data.groups.GroupState

interface GetGroupByLinkUseCase {
    fun getGroupByLink(link: String, callback: (GroupState) -> Unit)
}