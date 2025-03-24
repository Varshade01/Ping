package com.khrd.pingapp.firebase.authentication

import android.app.Activity
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.khrd.pingapp.login.LoginState

interface GoogleAuthAPI {
    fun loginViaGoogleAPI(activity: Activity, callback: (LoginState, BeginSignInResult?) -> Unit)
}