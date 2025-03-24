package com.khrd.pingapp

import android.content.Intent
import android.os.Bundle
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.authentication.ReloadError
import com.khrd.pingapp.firebase.authentication.ReloadStateFailure
import com.khrd.pingapp.firebase.authentication.ReloadStateSuccess
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.registration.RegistrationActivity
import com.khrd.pingapp.registration.verification.VerificationActivity
import com.khrd.pingapp.utils.messenger.AppReceivedPingsMessengerImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenActivity : PingAppBaseActivity() {
    @Inject
    lateinit var auth: FirebaseAuthAPI

    @Inject
    lateinit var connectionStatus: ConnectionStatus

    @Inject
    lateinit var receivedPingsMessenger: AppReceivedPingsMessengerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        connectionStatus.retrieveConnectionStatus {
            initialize()
        }
    }

    private fun initialize() {
        receivedPingsMessenger.startListening()

        if (auth.currentUserId() == null) {
            startNewActivity(RegistrationActivity::class.java)
        } else {
            auth.reload() { reloadState ->
                when (reloadState) {
                    ReloadStateSuccess -> {
                        startHomeScreen()
                    }
                    is ReloadStateFailure -> {
                        checkReloadStateErrors(reloadState)
                    }
                }
            }
        }
    }

    private fun checkReloadStateErrors(reloadState: ReloadStateFailure) {
        when (reloadState.error) {
            ReloadError.NetworkFailure -> {
                toastUtils.showNetworkErrorToast()
                startHomeScreen()
            }
            ReloadError.InvalidUserFailure -> {
                startNewActivity(RegistrationActivity::class.java)
            }
            ReloadError.UnknownFailure -> {
                toastUtils.showUnknownErrorToast()
                startNewActivity(RegistrationActivity::class.java)
            }
        }
    }

    private fun startHomeScreen() {
        checkEmailVerificationState(auth.isEmailVerified())
    }

    private fun checkEmailVerificationState(isEmailVerified: Boolean) {
        if (isEmailVerified) {
            startNewActivity(HomeScreen::class.java)
        } else {
            startNewActivity(VerificationActivity::class.java)
        }
    }

    private fun startNewActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        if (this.intent.data == null) {
            val id = this.intent.getStringExtra(Constants.GROUP_ID_FROM_DEEP_LINKING)
            intent.putExtra(Constants.GROUP_ID_FROM_DEEP_LINKING, id)
        } else {
            intent.putExtra(Constants.GROUP_ID_FROM_DEEP_LINKING, this.intent.data?.getQueryParameter(Constants.GROUP_ID_FROM_DEEP_LINKING))
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
