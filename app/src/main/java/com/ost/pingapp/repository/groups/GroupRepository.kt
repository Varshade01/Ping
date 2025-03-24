package com.khrd.pingapp.repository.groups

import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.data.pings.DataLoadFlag

interface GroupRepository {
    fun createGroup(uid: String, name: String?, callback: (GroupState) -> Unit)
    fun removeGroup(id: String, callback: (GroupState) -> Unit)
    fun updateGroupName(groupId: String, name: String, callback: (GroupState) -> Unit)
    fun addInvitationLinkToGroup(id: String, link: String, callback: (GroupState) -> Unit)
    fun getGroup(id: String, loadFlag: DataLoadFlag, callback: (GroupState) -> Unit)
    fun getGroupByLink(link: String, callback: (GroupState) -> Unit)
    fun getGroups(groupIds: List<String>, loadFlag: DataLoadFlag, callback: (List<DatabaseGroup>) -> Unit)
    fun updateGroupImage(groupId: String, bytes: ByteArray, callback: (GroupState) -> Unit)
    fun updateCachedGroupName(groupId: String, name: String)
}