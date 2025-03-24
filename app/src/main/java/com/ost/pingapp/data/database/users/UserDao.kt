package com.khrd.pingapp.data.database.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllUsers(user: List<UserEntity>)

    @Query("delete from users")
    fun deleteAllUsers()

    @Query("delete from users where userId ==:userId")
    fun deleteUser(userId: String)

    @Query("select * from users")
    fun getAllUsersAsList(): List<UserEntity>

    @Query("select * from users where userId ==:userId")
    fun getUsersByID(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE groups LIKE '%' || :groupId || '%'")
    fun getUsersByGroupId(groupId: String): List<UserEntity>

    @Query("UPDATE users SET groups =:updatedGroup WHERE userId ==:userId")
    fun updateGroups(userId: String, updatedGroup: HashMap<String, String>?)

    @Query("UPDATE users SET userName =:userName WHERE userId ==:userId")
    fun updateName(userId: String, userName: String)

    @Query("UPDATE users SET job =:updatedJob WHERE userId ==:userId")
    fun updateJob(userId: String, updatedJob: String)

    @Query("UPDATE users SET userEmail =:updatedEmail WHERE userId ==:userId")
    fun updateEmail(userId: String, updatedEmail: String)

    @Query("UPDATE users SET photoURL =:updatedPhoto WHERE userId ==:userId")
    fun updatePhoto(userId: String, updatedPhoto: String)

    @Query("UPDATE users SET fcmTokens =:updatedFcmToken WHERE userId ==:userId")
    fun updateFcmToken(userId: String, updatedFcmToken: String)

    @Query("UPDATE users SET isHide =:isHide WHERE userId ==:userId")
    fun hideUserInfo(userId: String, isHide: Boolean)

}