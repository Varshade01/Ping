package com.khrd.pingapp.datastore

interface DataStoreManager {
    suspend fun saveFcmToken(token: String)
    suspend fun getFcmToken(): String
    suspend fun saveCurrentGroup(currentGroupId: String)
    suspend fun getCurrentGroup(): String
    suspend fun saveCurrentLocale(language: String)
    suspend fun getCurrentLocale(): String?
    suspend fun getLastShownPing():String?
    suspend fun setShownPing(pingID: String)
}