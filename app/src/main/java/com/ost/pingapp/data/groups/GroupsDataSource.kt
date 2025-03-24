package com.khrd.pingapp.data.groups

interface GroupsDataSource {
    fun createGroup(userId: String, name: String?, callback: (GroupState) -> Unit)
    fun removeGroup(id: String, callback: (GroupState) -> Unit)
    fun updateGroupName(id: String, name: String, callback: (GroupState) -> Unit)
    fun addInvitationLinkToGroup(id: String, link: String, callback: (GroupState) -> Unit)
    fun getGroup(id: String, callback: (GroupState) -> Unit)
    fun getGroupByLink(link: String, callback: (GroupState) -> Unit)
    fun updateGroupImage(groupId: String, bytes: ByteArray, callback: (GroupState) -> Unit)
}