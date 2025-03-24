package com.khrd.pingapp.registration.verification

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.khrd.pingapp.PingAppBaseActivity
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.databinding.ActivityVerificationBinding
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.login.LoginActivity
import com.khrd.pingapp.registration.verification.logout.ILogoutState
import com.khrd.pingapp.registration.verification.logout.LogoutNetworkFailure
import com.khrd.pingapp.registration.verification.logout.LogoutSuccess
import com.khrd.pingapp.registration.verification.logout.LogoutUnknownFailure
import com.khrd.pingapp.registration.verification.sendEmail.*
import com.khrd.pingapp.utils.WarningDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationActivity : PingAppBaseActivity() {
    private lateinit var binding: ActivityVerificationBinding
    private val viewModel by viewModels<VerificationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
    }

    private fun initListeners() {
        viewModel.logoutStateLiveData.observe(this, { it ->
            handleLogoutState(it)
        })

        viewModel.sendConfirmationEmailLiveData.observe(this, { it ->
            handleConfirmEmailState(it)
        })

        viewModel.isEmailVerifiedLiveData.observe(this, { it ->
            handleLoginState(it)
        })

        binding.buttonConfirmed.setOnClickListener {
            viewModel.login()
        }

        binding.buttonSendVerificationLink.setOnClickListener {
            viewModel.sendConfirmationEmail()
        }

        binding.buttonLogout.setOnClickListener {
            showLogOutDialog()
        }
    }

    private fun handleLoginState(isEmailVerified: Boolean) {
        if (isEmailVerified) {
            val intent = Intent(this, HomeScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(Constants.GROUP_ID_FROM_DEEP_LINKING, this.intent.getStringExtra(Constants.GROUP_ID_FROM_DEEP_LINKING))
            startActivity(intent)
        } else {
            toastUtils.showToast(R.string.email_not_confirmed)
        }
    }

    private fun handleConfirmEmailState(it: ISendConfirmationEmailState?) {
        when (it) {
            is SendConfirmationEmailSuccess -> {
                toastUtils.showToast(R.string.confirm_email_send)
            }
            is SendConfirmationEmailNetworkFailure -> {
                toastUtils.showNetworkErrorToast()
            }
            is SendConfirmationEmailTooManyRequests -> {
                toastUtils.showTooManyRequestToast()
            }
            is SendConfirmationEmailUnknownFailure -> {
                toastUtils.showUnknownErrorToast()
            }
        }
    }

    private fun handleLogoutState(it: ILogoutState?) {
        when (it) {
            is LogoutSuccess -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            is LogoutNetworkFailure -> {
                toastUtils.showNetworkErrorToast()
            }
            is LogoutUnknownFailure -> {
                toastUtils.showUnknownErrorToast()
            }
            null -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.login()
    }

    private fun showLogOutDialog() {
            WarningDialog.show(
                context = this,
                title = getString(R.string.logout),
                message = getString(R.string.to_log_back_in),
                confirmButtonText = getString(R.string.logout),
                onConfirmClickListener = {viewModel.logout()}
            )
        }
}