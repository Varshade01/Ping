package com.khrd.pingapp.groupmanagement.usecases.createGroup

import com.khrd.pingapp.data.groups.GroupError
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupState
import com.khrd.pingapp.firebase.FirebaseDynamicLinkAPI
import com.khrd.pingapp.repository.groups.GroupRepository
import javax.inject.Inject

class GenerateLinkUseCaseImpl @Inject constructor(
    private val firebaseDynamicLink: FirebaseDynamicLinkAPI,
    private val groupRepository: GroupRepository
) : GenerateLinkUseCase {

    override fun generateLink(groupId: String, callback: (GroupState) -> Unit) {
        firebaseDynamicLink.generateShortDynamicLink(groupId) {
            if(it==null){
                callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            } else{
                groupRepository.addInvitationLinkToGroup(groupId,it.toString()){
                    groupState -> callback(groupState)
                }
            }
        }
    }
}