package com.khrd.pingapp.data.database.users

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.khrd.pingapp.data.database.TypeConverter

@Entity(tableName = "users")
data class UserEntity(

    @PrimaryKey @NonNull val userId: String,
    val userEmail: String? = null,
    var userName: String? = null,
    @TypeConverters(TypeConverter::class)
    val groups: HashMap<String, String>? = hashMapOf(),
    var job: String? = null,
    var photoURL: String? = null,
    @TypeConverters(TypeConverter::class)
    val fcmTokens: HashMap<String, String>? = hashMapOf(),
    val isDeleted: Boolean? = null,
    @TypeConverters(TypeConverter::class)
    val mutedItems: HashMap<String, String>? = hashMapOf(),
    val isHide: Boolean? = null,
)