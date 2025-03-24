package com.khrd.pingapp.data.database.groups

import com.khrd.pingapp.data.groups.DatabaseGroup
import javax.inject.Inject

class GroupMapper @Inject constructor() {

    fun mapToEntity(databaseGroup: DatabaseGroup) = GroupEntity(
        id = databaseGroup.id,
        name = databaseGroup.name,
        invitationLink = databaseGroup.invitationLink,
        imageUrl = databaseGroup.photoURL,
    )

    fun mapToFirebaseModel(groupEntity: GroupEntity) = DatabaseGroup(
        id = groupEntity.id,
        name = groupEntity.name,
        invitationLink = groupEntity.invitationLink,
        photoURL = groupEntity.imageUrl,
    )
}