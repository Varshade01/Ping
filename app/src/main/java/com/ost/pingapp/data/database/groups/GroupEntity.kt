package com.khrd.pingapp.data.database.groups

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Group_table")
data class GroupEntity(
    @PrimaryKey var id: String = "",
    var name: String? = "",
    var invitationLink: String? = "",
    var imageUrl: String? = "",
)