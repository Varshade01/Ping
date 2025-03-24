package com.khrd.pingapp.firebase.authentication

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.khrd.pingapp.constants.DbConstants
import com.khrd.pingapp.login.LoginState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GoogleAuth @Inject constructor(@ApplicationContext private val context: Context) : GoogleAuthAPI {

    var oneTapClient: SignInClient = Identity.getSignInClient(context)

    override fun loginViaGoogleAPI(activity: Activity, callback: (LoginState, BeginSignInResult?) -> Unit) {
        oneTapClient.beginSignIn(provideSignInRequest())
            .addOnSuccessListener(activity) { result ->
                callback(LoginState.LoginSuccess, result)
            }
            .addOnFailureListener(activity) {
                callback(LoginState.UnknownFailure, null)
            }
    }

    private fun provideSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(DbConstants.WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)// Show all accounts on the device.
                    .build()
            )
            .build()
    }
}