package com.khrd.pingapp.constants

import androidx.datastore.preferences.core.stringPreferencesKey

object DataStorePreferencesConstants {
    val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
    val CURRENT_GROUP = stringPreferencesKey("current_group")
    val CURRENT_LANGUAGE = stringPreferencesKey("current_language")
    val LAST_SHOWN_PING_KEY = stringPreferencesKey("last_shown_ping")
    const val INTOUCH_DATASTORE = "intouch_datastore"
}
