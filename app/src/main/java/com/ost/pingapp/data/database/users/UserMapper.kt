package com.khrd.pingapp.data.database.users

import com.khrd.pingapp.data.users.DatabaseUser
import javax.inject.Inject

class UserMapper @Inject constructor() {
    fun mapFirebaseResponseToDatabaseEntity(firebaseResponse: DatabaseUser) = UserEntity(
        userId = firebaseResponse.id ?: "",
        userEmail = firebaseResponse.email,
        userName = firebaseResponse.username,
        groups = firebaseResponse.groups,
        job = firebaseResponse.job,
        photoURL = firebaseResponse.photoURL,
        fcmTokens = firebaseResponse.fcmTokens,
        isDeleted = firebaseResponse.isDeleted,
        mutedItems = firebaseResponse.mutedItems,
        isHide = firebaseResponse.hideInfo
    )

    fun mapDatabaseEntityToFirebaseUser(databaseResponse: UserEntity) = DatabaseUser(
        id = databaseResponse.userId,
        email = databaseResponse.userEmail,
        username = databaseResponse.userName,
        groups = databaseResponse.groups,
        job = databaseResponse.job,
        photoURL = databaseResponse.photoURL,
        fcmTokens = databaseResponse.fcmTokens,
        isDeleted = databaseResponse.isDeleted,
        online = null,
        mutedItems = databaseResponse.mutedItems,
        hideInfo = databaseResponse.isHide ?: false
    )
}