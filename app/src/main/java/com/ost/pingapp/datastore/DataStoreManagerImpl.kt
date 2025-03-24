package com.khrd.pingapp.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.khrd.pingapp.constants.DataStorePreferencesConstants.CURRENT_GROUP
import com.khrd.pingapp.constants.DataStorePreferencesConstants.CURRENT_LANGUAGE
import com.khrd.pingapp.constants.DataStorePreferencesConstants.FCM_TOKEN_KEY
import com.khrd.pingapp.constants.DataStorePreferencesConstants.INTOUCH_DATASTORE
import com.khrd.pingapp.constants.DataStorePreferencesConstants.LAST_SHOWN_PING_KEY
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = INTOUCH_DATASTORE)

class DataStoreManagerImpl(
    val context: Context
) : DataStoreManager {

    override suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { store -> store[FCM_TOKEN_KEY] = token }
    }

    override suspend fun getFcmToken(): String = context.dataStore.data.map { preferences -> preferences[FCM_TOKEN_KEY] ?: "" }.first()

    override suspend fun saveCurrentGroup(currentGroupId: String) {
        context.dataStore.edit { store -> store[CURRENT_GROUP] = currentGroupId }
    }

    override suspend fun getCurrentGroup(): String = context.dataStore.data.map { preferences -> preferences[CURRENT_GROUP] ?: "" }.first()

    override suspend fun saveCurrentLocale(language: String) {
        context.dataStore.edit { store -> store[CURRENT_LANGUAGE] = language }
    }

    override suspend fun getCurrentLocale(): String? = context.dataStore.data.map { preferences -> preferences[CURRENT_LANGUAGE] }.first()
    override suspend fun setShownPing(pingID: String) {
        context.dataStore.edit { store ->
            store[LAST_SHOWN_PING_KEY] = pingID
        }
    }

    override suspend fun getLastShownPing(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_SHOWN_PING_KEY]
        }.firstOrNull()
    }

}