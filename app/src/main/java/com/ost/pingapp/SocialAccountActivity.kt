package com.khrd.pingapp

import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.khrd.pingapp.firebase.authentication.GoogleAuth
import com.khrd.pingapp.login.LoginActivity
import com.khrd.pingapp.login.LoginState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class SocialAccountActivity : PingAppBaseActivity() {

    @Inject
    lateinit var googleAuth: GoogleAuth

    fun loginViaGoogle() {
        googleAuth.loginViaGoogleAPI(this) { result, sighIn ->
            if (result == LoginState.LoginSuccess) {
                sighIn?.let { handleGoogleAccountsTabOpening(it) }
            } else {
                toastUtils.showNoGoogleAccountsFoundToast()
            }
        }
    }

    private fun handleGoogleAccountsTabOpening(result: BeginSignInResult) {
        try {
            startIntentSender(result)
        } catch (e: IntentSender.SendIntentException) {
            toastUtils.showUnknownErrorToast()
        }
    }

    private fun startIntentSender(result: BeginSignInResult) {
        startIntentSenderForResult(
            result.pendingIntent.intentSender, LoginActivity.REQ_ONE_TAP,
            null, 0, 0, 0
        )
    }
}