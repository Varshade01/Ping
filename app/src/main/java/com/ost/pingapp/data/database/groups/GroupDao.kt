package com.khrd.pingapp.data.database.groups

import androidx.room.*

@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupToDb(group: GroupEntity)

    @Query("SELECT * FROM Group_table WHERE id =:groupId")
    fun getGroupFromDb(groupId: String): GroupEntity?

    @Query("SELECT * FROM Group_table WHERE invitationLink =:link")
    fun getGroupFromDbByLink(link: String): GroupEntity?

    @Query("UPDATE Group_table SET name=:newName WHERE id ==:groupId")
    fun updateGroupName(groupId: String, newName: String)

    @Query("UPDATE Group_table SET imageUrl =:imageUrl WHERE id ==:groupId")
    fun updateGroupImage(groupId: String, imageUrl: String)

    @Query("DELETE FROM Group_table WHERE id=:id")
    fun deleteGroup(id: String)

}